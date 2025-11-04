import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

/**
 * 後端打包構建器
 * 負責將 Spring Boot 專案打包成可執行 JAR
 */
export async function buildBackend() {
  console.log('[Backend Builder] 開始構建後端專案');

  const srcPath = '/src/backend';
  const workspacePath = `/tmp/build-backend-${Date.now()}`;
  const outputPath = '/output/backend';
  const jarName = 'app.jar';

  try {
    // 檢查源碼目錄
    if (!fs.existsSync(srcPath)) {
      throw new Error(`後端源碼目錄不存在: ${srcPath}`);
    }

    console.log('[Backend Builder] 檢查 pom.xml...');
    const pomPath = path.join(srcPath, 'pom.xml');
    if (!fs.existsSync(pomPath)) {
      throw new Error(`pom.xml 不存在: ${pomPath}`);
    }

    // 準備工作區
    console.log(`[Backend Builder] 準備工作區: ${workspacePath}`);
    execSync(`cp -r ${srcPath} ${workspacePath}`, { stdio: 'inherit' });

    // 執行 Maven 清理和打包
    console.log('[Backend Builder] 執行 Maven 打包 (mvn clean package -DskipTests)...');
    console.log('[Backend Builder] 首次構建需下載依賴，可能需要數分鐘，請耐心等待...');

    const startTime = Date.now();

    execSync('mvn clean package -DskipTests --batch-mode', {
      cwd: workspacePath,
      stdio: 'inherit',
      env: {
        ...process.env,
        MAVEN_OPTS: '-Xmx2g -Xms512m -Djava.awt.headless=true'
      }
    });

    const buildDuration = ((Date.now() - startTime) / 1000).toFixed(2);
    console.log(`[Backend Builder] Maven 打包完成，耗時: ${buildDuration} 秒`);

    // 尋找生成的 JAR 檔案
    console.log('[Backend Builder] 檢查構建產物...');
    const targetPath = path.join(workspacePath, 'target');

    if (!fs.existsSync(targetPath)) {
      throw new Error('構建失敗：target 目錄不存在');
    }

    // 尋找 JAR 檔案（排除 .original 檔案）
    const jarFiles = fs.readdirSync(targetPath)
      .filter(f => f.endsWith('.jar') && !f.endsWith('.jar.original'))
      .sort((a, b) => {
        // 優先選擇不含 'sources' 和 'javadoc' 的檔案
        const aScore = (a.includes('sources') || a.includes('javadoc')) ? 1 : 0;
        const bScore = (b.includes('sources') || b.includes('javadoc')) ? 1 : 0;
        return aScore - bScore;
      });

    if (jarFiles.length === 0) {
      throw new Error('構建失敗：找不到生成的 JAR 檔案');
    }

    const builtJarPath = path.join(targetPath, jarFiles[0]);
    console.log(`[Backend Builder] 找到 JAR 檔案: ${jarFiles[0]}`);

    // 獲取 JAR 檔案大小
    const jarStats = fs.statSync(builtJarPath);
    const jarSizeMB = (jarStats.size / (1024 * 1024)).toFixed(2);
    console.log(`[Backend Builder] JAR 檔案大小: ${jarSizeMB} MB`);

    // 確保輸出目錄存在
    execSync(`mkdir -p ${outputPath}`);

    // 複製 JAR 到輸出目錄
    const outputJarPath = path.join(outputPath, jarName);
    console.log(`[Backend Builder] 複製 JAR 到: ${outputJarPath}`);
    execSync(`cp ${builtJarPath} ${outputJarPath}`);

    // 寫入構建資訊
    const buildInfo = {
      project: 'backend',
      type: 'spring-boot-jar',
      timestamp: new Date().toISOString(),
      buildDuration: `${buildDuration}s`,
      jarPath: outputJarPath,
      jarSize: `${jarSizeMB} MB`,
      originalJarName: jarFiles[0],
      maven: {
        command: 'mvn clean package -DskipTests',
        javaVersion: process.env.JAVA_HOME || 'unknown',
        mavenOpts: '-Xmx2g -Xms512m'
      }
    };

    const buildInfoPath = path.join(outputPath, '.build-info.json');
    fs.writeFileSync(buildInfoPath, JSON.stringify(buildInfo, null, 2));
    console.log(`[Backend Builder] 構建資訊已寫入: ${buildInfoPath}`);

    console.log('[Backend Builder] ✓ 後端構建完成！');
    console.log(`[Backend Builder] JAR 檔案位置: ${outputJarPath}`);

    return buildInfo;

  } catch (error) {
    console.error('[Backend Builder] ✗ 構建失敗:', error.message);
    throw error;
  } finally {
    // 清理工作區
    if (fs.existsSync(workspacePath)) {
      console.log('[Backend Builder] 清理工作區...');
      execSync(`rm -rf ${workspacePath}`);
    }
  }
}

/**
 * 檢查後端構建環境
 */
export function checkBackendEnvironment() {
  const checks = {
    javaVersion: null,
    mavenVersion: null,
    sourceExists: false,
    outputPathWritable: false
  };

  try {
    // 檢查 Java 版本
    const javaVersion = execSync('java -version 2>&1 | head -n 1', {
      encoding: 'utf-8'
    }).trim();
    checks.javaVersion = javaVersion;
  } catch (error) {
    checks.javaVersion = 'Java 未安裝或不在 PATH 中';
  }

  try {
    // 檢查 Maven 版本
    const mavenVersion = execSync('mvn -version 2>&1 | head -n 1', {
      encoding: 'utf-8'
    }).trim();
    checks.mavenVersion = mavenVersion;
  } catch (error) {
    checks.mavenVersion = 'Maven 未安裝或不在 PATH 中';
  }

  // 檢查源碼目錄
  checks.sourceExists = fs.existsSync('/src/backend');

  // 檢查輸出目錄可寫性
  try {
    const testFile = '/output/backend/.write-test';
    execSync(`mkdir -p /output/backend && touch ${testFile} && rm ${testFile}`);
    checks.outputPathWritable = true;
  } catch (error) {
    checks.outputPathWritable = false;
  }

  return checks;
}
