import { execSync } from 'child_process';
import fs from 'fs';

export async function deployFrontend(projectName) {
  console.log(`[Deployer] 開始部署: ${projectName}`);

  // 讀取配置
  const configPath = `/app/config/${projectName}.json`;
  if (!fs.existsSync(configPath)) {
    throw new Error(`配置文件不存在: ${configPath}`);
  }

  const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));
  const distPath = `/dist/${projectName}/dist`;

  // 檢查產物目錄
  if (!fs.existsSync(distPath)) {
    throw new Error(`構建產物不存在，請先執行構建: ${distPath}`);
  }

  // 執行 Wrangler 部署
  console.log(`[Deployer] 部署到 Cloudflare Pages: ${config.pagesProjectName}`);

  const cmd = [
    'npx wrangler pages deploy',
    distPath,
    `--project-name="${config.pagesProjectName}"`,
    '--branch=main',
    '--commit-dirty=true'
  ].join(' ');

  try {
    execSync(cmd, {
      encoding: 'utf8',
      stdio: 'inherit'
    });
  } catch (error) {
    throw new Error(`Wrangler 部署失敗: ${error.message}`);
  }

  const deployInfo = {
    project: projectName,
    timestamp: new Date().toISOString(),
    pagesUrl: `https://${config.pagesProjectName}.pages.dev`,
    customDomain: config.customDomain ? `https://${config.customDomain}` : null
  };

  // 寫入部署資訊
  fs.writeFileSync(
    `/dist/${projectName}/.deploy-info.json`,
    JSON.stringify(deployInfo, null, 2)
  );

  console.log(`[Deployer] 部署完成: ${projectName}`);

  return deployInfo;
}
