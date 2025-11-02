#!/usr/bin/env node

import { execSync, spawn } from 'child_process';
import { readFileSync, writeFileSync, existsSync, mkdirSync, chmodSync, renameSync } from 'fs';
import { generateKeyPairSync } from 'crypto';
import { join, resolve } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// ANSI 顏色碼
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  cyan: '\x1b[36m',
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function error(message) {
  log(`✗ 錯誤: ${message}`, 'red');
}

function success(message) {
  log(`✓ ${message}`, 'green');
}

function info(message) {
  log(`ℹ ${message}`, 'cyan');
}

function warn(message) {
  log(`⚠ ${message}`, 'yellow');
}

// 檢查 ssh-keygen 是否可用
function isSshKeygenAvailable() {
  try {
    // 使用 --help 來檢測 ssh-keygen 是否存在
    // ssh-keygen --help 會輸出 usage 並返回 exit code 1
    execSync('ssh-keygen --help 2>&1', { encoding: 'utf8', stdio: 'pipe' });
    return true;
  } catch (error) {
    // 即使 exit code 是 1，只要有 usage 輸出就表示命令存在
    if (error.stdout && error.stdout.includes('usage:')) {
      return true;
    }
    return false;
  }
}

// 使用 ssh-keygen 生成金鑰
function generateWithSshKeygen(config, keyPath, pubKeyPath, keyType) {
  const { name, bits, comment, passphrase } = config;

  const typeArg = keyType === 'ed25519' ? 'ed25519' : 'rsa';
  const bitsArg = keyType === 'rsa' ? ['-b', bits || '4096'] : [];

  // ssh-keygen 使用不帶副檔名的基礎名稱
  // 它會自動生成 {basename} 和 {basename}.pub
  const outputDir = dirname(keyPath);
  const basename = name;
  const tempKeyPath = join(outputDir, basename);
  const tempPubPath = `${tempKeyPath}.pub`;

  const args = [
    '-t', typeArg,
    ...bitsArg,
    '-C', `"${comment || name}"`,     // 加引號避免空格問題
    '-f', `"${tempKeyPath}"`,          // 使用臨時基礎名稱
    '-N', `"${passphrase || ''}"`,     // 密碼加引號
    '-q', // Quiet mode
  ];

  try {
    execSync(`ssh-keygen ${args.join(' ')}`, { stdio: 'pipe' });

    // 重新命名為我們要的檔名格式
    // tempKeyPath -> keyPath (加上 .pem)
    // tempPubPath -> pubKeyPath (保持 .pub)
    if (existsSync(tempKeyPath)) {
      renameSync(tempKeyPath, keyPath);
    }
    if (existsSync(tempPubPath)) {
      renameSync(tempPubPath, pubKeyPath);
    }

    return true;
  } catch (err) {
    warn(`ssh-keygen 失敗: ${err.message}`);
    return false;
  }
}

// 使用 Node.js crypto 生成金鑰
function generateWithCrypto(config, keyPath, pubKeyPath, keyType) {
  const { name, bits, comment, passphrase } = config;

  try {
    let keyPair;

    if (keyType === 'ed25519') {
      keyPair = generateKeyPairSync('ed25519', {
        privateKeyEncoding: {
          type: 'pkcs8',
          format: 'pem',
          ...(passphrase && {
            cipher: 'aes-256-cbc',
            passphrase: passphrase
          })
        },
        publicKeyEncoding: {
          type: 'spki',
          format: 'pem'
        }
      });
    } else if (keyType === 'rsa') {
      keyPair = generateKeyPairSync('rsa', {
        modulusLength: bits || 4096,
        privateKeyEncoding: {
          type: 'pkcs8',
          format: 'pem',
          ...(passphrase && {
            cipher: 'aes-256-cbc',
            passphrase: passphrase
          })
        },
        publicKeyEncoding: {
          type: 'pkcs1',
          format: 'pem'
        }
      });
    } else {
      throw new Error(`不支援的金鑰類型: ${keyType}`);
    }

    // 寫入私鑰
    writeFileSync(keyPath, keyPair.privateKey, { mode: 0o400 });

    // 轉換公鑰為 OpenSSH 格式
    const publicKeySSH = convertToOpenSSHFormat(keyPair.publicKey, keyType, comment || name);
    writeFileSync(pubKeyPath, publicKeySSH);

    return true;
  } catch (err) {
    error(`Crypto 生成失敗: ${err.message}`);
    return false;
  }
}

// 將 PEM 格式公鑰轉換為 OpenSSH 格式
function convertToOpenSSHFormat(pemKey, type, comment) {
  // 移除 PEM 標頭和換行
  const base64Key = pemKey
    .replace(/-----BEGIN PUBLIC KEY-----/, '')
    .replace(/-----END PUBLIC KEY-----/, '')
    .replace(/-----BEGIN RSA PUBLIC KEY-----/, '')
    .replace(/-----END RSA PUBLIC KEY-----/, '')
    .replace(/\n/g, '')
    .trim();

  const keyType = type === 'ed25519' ? 'ssh-ed25519' : 'ssh-rsa';

  // 對於 RSA，需要額外處理
  if (type === 'rsa') {
    // 這裡簡化處理，實際上 ssh-keygen 生成的格式更標準
    return `${keyType} ${base64Key} ${comment}`;
  }

  return `${keyType} ${base64Key} ${comment}`;
}

// 生成單個金鑰
function generateKey(config, defaults = {}) {
  const { name, type, output_dir } = config;

  // 使用預設值
  const keyType = type || defaults.default_key_type || 'ed25519';
  const outputDir = output_dir || defaults.default_output_dir || './output';

  log(`\n開始生成金鑰: ${name}`, 'bright');
  info(`金鑰類型: ${keyType.toUpperCase()}`);

  // 確保輸出目錄存在
  const outputPath = resolve(__dirname, outputDir);
  if (!existsSync(outputPath)) {
    mkdirSync(outputPath, { recursive: true });
  }

  // 使用 .pem 作為私鑰副檔名
  const keyPath = join(outputPath, `${name}.pem`);
  const pubKeyPath = join(outputPath, `${name}.pub`);
  const terraformPath = join(outputPath, `${name}.terraform.txt`);

  // 檢查是否已存在
  if (existsSync(keyPath)) {
    warn(`金鑰已存在: ${keyPath}`);
    const shouldOverwrite = true; // 在實際應用中可以詢問使用者
    if (!shouldOverwrite) {
      info('跳過生成');
      return false;
    }
  }

  // 合併配置，確保有 keyType
  const fullConfig = { ...config, type: keyType };

  // 選擇生成方法
  const useSshKeygen = isSshKeygenAvailable();
  let generated = false;

  if (useSshKeygen) {
    info('使用 ssh-keygen 生成金鑰...');
    generated = generateWithSshKeygen(fullConfig, keyPath, pubKeyPath, keyType);
  }

  if (!generated) {
    info('使用 Node.js crypto 生成金鑰...');
    generated = generateWithCrypto(fullConfig, keyPath, pubKeyPath, keyType);
  }

  if (!generated) {
    error(`無法生成金鑰: ${name}`);
    return false;
  }

  // 讀取公鑰並生成 Terraform 格式
  try {
    const publicKey = readFileSync(pubKeyPath, 'utf-8').trim();
    writeFileSync(terraformPath, publicKey);

    // 設定私鑰權限 (Unix-like 系統)
    try {
      chmodSync(keyPath, 0o400);
    } catch (err) {
      // Windows 可能不支援 chmod
      if (process.platform !== 'win32') {
        warn('無法設定私鑰權限');
      }
    }

    success(`金鑰生成成功!`);
    info(`私鑰: ${keyPath}`);
    info(`公鑰: ${pubKeyPath}`);
    info(`Terraform: ${terraformPath}`);

    return true;
  } catch (err) {
    error(`處理金鑰檔案失敗: ${err.message}`);
    return false;
  }
}

// 讀取配置檔案
function loadConfig() {
  const configPath = join(__dirname, 'config.json');

  if (!existsSync(configPath)) {
    error('找不到 config.json');
    info('請先複製 config.json.example 並修改設定:');
    info('  cp config.json.example config.json');
    process.exit(1);
  }

  try {
    const configData = readFileSync(configPath, 'utf-8');
    return JSON.parse(configData);
  } catch (err) {
    error(`讀取配置檔案失敗: ${err.message}`);
    process.exit(1);
  }
}

// 驗證配置
function validateConfig(config) {
  if (!config.keys || !Array.isArray(config.keys)) {
    error('配置檔案格式錯誤: 缺少 keys 陣列');
    return false;
  }

  // 驗證預設金鑰類型（如果有設定）
  if (config.default_key_type && !['ed25519', 'rsa'].includes(config.default_key_type)) {
    error(`配置錯誤: 不支援的預設金鑰類型 "${config.default_key_type}" (僅支援 ed25519 或 rsa)`);
    return false;
  }

  const defaultType = config.default_key_type || 'ed25519';

  for (const keyConfig of config.keys) {
    if (!keyConfig.name) {
      error('配置錯誤: 缺少金鑰名稱 (name)');
      return false;
    }

    const keyType = keyConfig.type || defaultType;

    if (!['ed25519', 'rsa'].includes(keyType)) {
      error(`配置錯誤: 不支援的金鑰類型 "${keyType}" (僅支援 ed25519 或 rsa)`);
      return false;
    }

    if (keyType === 'rsa') {
      const bits = keyConfig.bits || 4096;
      if (bits < 2048) {
        warn(`金鑰 "${keyConfig.name}": RSA 位元數過低 (${bits})，建議至少 4096`);
      }
    }
  }

  return true;
}

// 主程式
function main() {
  log('\n===========================================', 'cyan');
  log('  MimiMart SSH Key Generator', 'bright');
  log('===========================================\n', 'cyan');

  // 讀取配置
  const config = loadConfig();

  // 驗證配置
  if (!validateConfig(config)) {
    process.exit(1);
  }

  // 檢查生成方法
  const useSshKeygen = isSshKeygenAvailable();
  if (useSshKeygen) {
    success('已檢測到 ssh-keygen 工具');
  } else {
    warn('未檢測到 ssh-keygen，將使用 Node.js crypto 模組');
  }

  // 提取預設值
  const defaults = {
    default_key_type: config.default_key_type || 'ed25519',
    default_output_dir: config.default_output_dir || './output'
  };

  // 生成所有金鑰
  let successCount = 0;
  let totalCount = config.keys.length;

  for (const keyConfig of config.keys) {
    if (generateKey(keyConfig, defaults)) {
      successCount++;
    }
  }

  // 總結
  log('\n===========================================', 'cyan');
  log(`完成! 成功生成 ${successCount}/${totalCount} 個金鑰`, 'bright');
  log('===========================================\n', 'cyan');

  if (successCount < totalCount) {
    process.exit(1);
  }
}

// 執行主程式
main();
