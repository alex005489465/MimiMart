import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

/**
 * 網關部署器
 * 負責將 Cloudflare Tunnel 和 Nginx 配置同步到 EC2 並重啟服務
 */
export async function deployGateway() {
  console.log('[Gateway Deployer] 開始部署網關配置到 EC2...');

  try {
    // 讀取配置文件
    const configPath = '/app/config/gateway.json';
    if (!fs.existsSync(configPath)) {
      throw new Error(`配置文件不存在: ${configPath}\n請複製 gateway.example.json 並填入實際值`);
    }

    const config = JSON.parse(fs.readFileSync(configPath, 'utf-8'));

    // 驗證配置
    validateConfig(config);

    // 檢查 SSH 金鑰
    if (!fs.existsSync(config.ec2.sshKeyPath)) {
      throw new Error(`SSH 金鑰不存在: ${config.ec2.sshKeyPath}`);
    }

    // 檢查源配置目錄
    const cloudflaredSource = '/gateway-config/cloudflared/config';
    const nginxSource = '/gateway-config/nginx/conf.d';

    if (!fs.existsSync(cloudflaredSource)) {
      throw new Error(`Cloudflare Tunnel 配置目錄不存在: ${cloudflaredSource}`);
    }

    if (!fs.existsSync(nginxSource)) {
      throw new Error(`Nginx 配置目錄不存在: ${nginxSource}`);
    }

    const sshHost = `${config.ec2.user}@${config.ec2.host}`;
    const sshOptions = `-i ${config.ec2.sshKeyPath} -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null`;

    console.log(`[Gateway Deployer] 目標主機: ${sshHost}`);

    // 1. 同步 Cloudflare Tunnel 配置
    console.log('[Gateway Deployer] 同步 Cloudflare Tunnel 配置...');
    syncDirectory(
      cloudflaredSource,
      `${sshHost}:${config.paths.cloudflaredConfig}`,
      sshOptions
    );

    // 2. 同步 Nginx 配置
    console.log('[Gateway Deployer] 同步 Nginx 配置...');
    syncDirectory(
      nginxSource,
      `${sshHost}:${config.paths.nginxConfig}`,
      sshOptions
    );

    // 3. 驗證 Nginx 配置語法
    console.log('[Gateway Deployer] 驗證 Nginx 配置語法...');
    const nginxTestCmd = `ssh ${sshOptions} ${sshHost} "docker exec mimimart-gateway-nginx nginx -t"`;
    sshExecute(nginxTestCmd);

    // 4. 重啟容器
    console.log('[Gateway Deployer] 重啟網關服務...');
    const restartCmd = `ssh ${sshOptions} ${sshHost} "cd ~/gateway && docker-compose restart ${config.services.gateway.containers.join(' ')}"`;
    sshExecute(restartCmd);

    // 5. 等待服務啟動
    console.log('[Gateway Deployer] 等待服務啟動...');
    await sleep(5000);

    // 生成部署資訊
    const deployInfo = {
      timestamp: new Date().toISOString(),
      target: sshHost,
      services: config.services.gateway.containers,
      status: 'success',
      message: '網關配置已同步並重啟服務'
    };

    console.log('[Gateway Deployer] ✓ 網關配置部署完成！');
    return deployInfo;

  } catch (error) {
    console.error('[Gateway Deployer] ✗ 部署失敗:', error.message);
    throw error;
  }
}

/**
 * 使用 rsync 同步目錄
 */
function syncDirectory(source, destination, sshOptions) {
  try {
    // 確保源目錄以 / 結尾（rsync 語法）
    const normalizedSource = source.endsWith('/') ? source : `${source}/`;

    const rsyncCommand = `rsync -avz --delete -e "ssh ${sshOptions}" ${normalizedSource} ${destination}`;

    console.log(`[Gateway Deployer] 執行: rsync ${normalizedSource} -> ${destination}`);

    execSync(rsyncCommand, {
      stdio: 'inherit',
      maxBuffer: 10 * 1024 * 1024  // 10MB buffer
    });

    console.log(`[Gateway Deployer] ✓ 同步完成`);
  } catch (error) {
    throw new Error(`rsync 失敗: ${error.message}`);
  }
}

/**
 * 透過 SSH 執行遠端命令
 * @param {string} sshCommand - 完整的 SSH 命令（已包含 ssh、選項、主機、遠端命令）
 */
function sshExecute(sshCommand) {
  try {
    const output = execSync(sshCommand, {
      encoding: 'utf-8',
      maxBuffer: 10 * 1024 * 1024
    });

    return output;
  } catch (error) {
    throw new Error(`SSH 命令執行失敗: ${error.message}`);
  }
}

/**
 * 驗證配置完整性
 */
function validateConfig(config) {
  const required = [
    'ec2.host',
    'ec2.user',
    'ec2.sshKeyPath',
    'paths.cloudflaredConfig',
    'paths.nginxConfig',
    'services.gateway.containers'
  ];

  for (const field of required) {
    const keys = field.split('.');
    let value = config;

    for (const key of keys) {
      value = value?.[key];
      if (value === undefined) {
        throw new Error(`配置缺少必要欄位: ${field}`);
      }
    }
  }

  // 驗證容器列表
  if (!Array.isArray(config.services.gateway.containers) ||
      config.services.gateway.containers.length === 0) {
    throw new Error('services.gateway.containers 必須是非空陣列');
  }
}

/**
 * 檢查網關部署環境
 */
export function checkGatewayEnvironment() {
  const checks = {
    configExists: false,
    rsyncInstalled: false,
    sshInstalled: false,
    sourceDirectoriesExist: false
  };

  try {
    // 檢查配置文件
    checks.configExists = fs.existsSync('/app/config/gateway.json');
  } catch (error) {
    checks.configExists = false;
  }

  try {
    // 檢查 rsync
    execSync('which rsync', { stdio: 'ignore' });
    checks.rsyncInstalled = true;
  } catch (error) {
    checks.rsyncInstalled = false;
  }

  try {
    // 檢查 SSH
    execSync('which ssh', { stdio: 'ignore' });
    checks.sshInstalled = true;
  } catch (error) {
    checks.sshInstalled = false;
  }

  // 檢查源目錄
  checks.sourceDirectoriesExist =
    fs.existsSync('/gateway-config/cloudflared/config') &&
    fs.existsSync('/gateway-config/nginx/conf.d');

  return checks;
}

/**
 * 輔助函數：延遲
 */
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
