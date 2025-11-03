import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

export async function buildFrontend(projectName) {
  console.log(`[Builder] 開始構建: ${projectName}`);

  const srcPath = `/src/${projectName}`;
  const workspacePath = `/tmp/build-${projectName}-${Date.now()}`;
  const distPath = `/dist/${projectName}`;

  try {
    // 檢查源碼目錄
    if (!fs.existsSync(srcPath)) {
      throw new Error(`源碼目錄不存在: ${srcPath}`);
    }

    // 準備工作區
    console.log(`[Builder] 準備工作區: ${workspacePath}`);
    execSync(`cp -r ${srcPath} ${workspacePath}`, { stdio: 'inherit' });

    // 安裝依賴
    console.log(`[Builder] 安裝依賴...`);
    execSync('npm ci --cache /root/.npm', {
      cwd: workspacePath,
      stdio: 'inherit'
    });

    // 執行構建
    console.log(`[Builder] 執行構建...`);
    execSync('npm run build:prod', {
      cwd: workspacePath,
      stdio: 'inherit'
    });

    // 檢查構建產物
    const buildDistPath = path.join(workspacePath, 'dist');
    if (!fs.existsSync(buildDistPath)) {
      throw new Error('構建失敗：dist 目錄不存在');
    }

    // 複製產物到掛載目錄
    console.log(`[Builder] 複製產物到: ${distPath}`);
    execSync(`mkdir -p ${distPath}`);
    execSync(`rm -rf ${distPath}/dist`);
    execSync(`cp -r ${buildDistPath} ${distPath}/`);

    // 寫入構建資訊
    const buildInfo = {
      project: projectName,
      timestamp: new Date().toISOString(),
      distPath: `${distPath}/dist`
    };
    fs.writeFileSync(
      `${distPath}/.build-info.json`,
      JSON.stringify(buildInfo, null, 2)
    );

    console.log(`[Builder] 構建完成: ${projectName}`);

    return buildInfo;

  } finally {
    // 清理工作區
    if (fs.existsSync(workspacePath)) {
      console.log(`[Builder] 清理工作區...`);
      execSync(`rm -rf ${workspacePath}`);
    }
  }
}
