#!/usr/bin/env node

/**
 * Cloudflare Tunnel Credentials Generator
 *
 * 將 CLOUDFLARE_TUNNEL_TOKEN 轉換為 credentials.json 檔案
 * 輸出位置: ../cloudflared/config/credentials.json
 *
 * Token 格式:
 * {
 *   "a": "account_id",
 *   "t": "tunnel_id",
 *   "s": "secret"
 * }
 *
 * Credentials 格式:
 * {
 *   "AccountTag": "account_id",
 *   "TunnelID": "tunnel_id",
 *   "TunnelSecret": "secret"
 * }
 */

const fs = require('fs');
const path = require('path');

// 輸出檔案路徑（相對於此腳本）
const OUTPUT_PATH = path.join(__dirname, '../cloudflared/config/credentials.json');

// Token 文件路徑（同目錄下）
const TOKEN_FILE_PATH = path.join(__dirname, '.env.token');

/**
 * 從 .env.token 文件讀取環境變數
 */
function loadEnvTokenFile() {
  if (!fs.existsSync(TOKEN_FILE_PATH)) {
    return {};
  }

  const content = fs.readFileSync(TOKEN_FILE_PATH, 'utf-8');
  const env = {};

  // 解析 KEY=VALUE 格式
  content.split('\n').forEach(line => {
    // 移除首尾空白
    line = line.trim();

    // 忽略空行和註解
    if (!line || line.startsWith('#')) {
      return;
    }

    // 解析 KEY=VALUE
    const match = line.match(/^([^=]+)=(.*)$/);
    if (match) {
      const key = match[1].trim();
      let value = match[2].trim();

      // 移除引號（如果有）
      if ((value.startsWith('"') && value.endsWith('"')) ||
          (value.startsWith("'") && value.endsWith("'"))) {
        value = value.slice(1, -1);
      }

      env[key] = value;
    }
  });

  return env;
}

/**
 * 讀取 Token
 * 優先順序：環境變數 > .env.token 文件
 */
function getToken() {
  // 1. 先檢查環境變數
  let token = process.env.CLOUDFLARE_TUNNEL_TOKEN;

  // 2. 如果沒有，讀取 .env.token 文件
  if (!token) {
    const envVars = loadEnvTokenFile();
    token = envVars.CLOUDFLARE_TUNNEL_TOKEN;
  }

  // 3. 如果都沒有，顯示錯誤
  if (!token) {
    console.error('❌ 錯誤: 未找到 CLOUDFLARE_TUNNEL_TOKEN');
    console.error('');
    console.error('請執行以下步驟：');
    console.error('1. 複製範本文件');
    console.error('   cp .env.token.example .env.token');
    console.error('');
    console.error('2. 編輯 .env.token 並填入 Token');
    console.error('   CLOUDFLARE_TUNNEL_TOKEN=your_token_here');
    console.error('');
    console.error('3. 重新執行此腳本');
    console.error('   node init-credentials.js');
    console.error('');
    console.error('或者直接設定環境變數：');
    console.error('   export CLOUDFLARE_TUNNEL_TOKEN=your_token');
    process.exit(1);
  }

  return token;
}

/**
 * 解碼 Token
 * 處理兩種格式：
 * 1. Base64 編碼的 JSON
 * 2. 純 JSON 字串
 */
function decodeToken(token) {
  try {
    // 嘗試直接解析 JSON
    return JSON.parse(token);
  } catch (e) {
    // 如果失敗，嘗試 base64 解碼後再解析
    try {
      const decoded = Buffer.from(token, 'base64').toString('utf-8');
      return JSON.parse(decoded);
    } catch (e2) {
      console.error('❌ 錯誤: 無法解析 Token');
      console.error('Token 必須是以下格式之一：');
      console.error('1. Base64 編碼的 JSON');
      console.error('2. 純 JSON 字串: {"a":"...","t":"...","s":"..."}');
      console.error('');
      console.error('解析錯誤:', e2.message);
      process.exit(1);
    }
  }
}

/**
 * 提取並驗證欄位
 */
function extractFields(tokenData) {
  const accountId = tokenData.a;
  const tunnelId = tokenData.t;
  const secret = tokenData.s;

  const missing = [];
  if (!accountId) missing.push('a (Account ID)');
  if (!tunnelId) missing.push('t (Tunnel ID)');
  if (!secret) missing.push('s (Secret)');

  if (missing.length > 0) {
    console.error('❌ 錯誤: Token 缺少必要欄位:', missing.join(', '));
    console.error('');
    console.error('Token 應包含以下欄位：');
    console.error('- "a": Cloudflare Account ID');
    console.error('- "t": Tunnel ID');
    console.error('- "s": Tunnel Secret');
    process.exit(1);
  }

  return { accountId, tunnelId, secret };
}

/**
 * 生成 credentials.json
 */
function generateCredentials(accountId, tunnelId, secret) {
  return {
    AccountTag: accountId,
    TunnelID: tunnelId,
    TunnelSecret: secret
  };
}

/**
 * 檢查檔案是否已存在
 */
function checkExisting() {
  if (fs.existsSync(OUTPUT_PATH)) {
    console.log('⚠️  credentials.json 已存在:', OUTPUT_PATH);
    console.log('');
    console.log('如需重新生成，請先刪除現有檔案：');
    console.log(`rm "${OUTPUT_PATH}"`);
    return true;
  }
  return false;
}

/**
 * 寫入檔案
 */
function writeCredentials(credentials) {
  // 確保目錄存在
  const dir = path.dirname(OUTPUT_PATH);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  // 寫入 JSON（格式化）
  const content = JSON.stringify(credentials, null, 2);
  fs.writeFileSync(OUTPUT_PATH, content, 'utf-8');

  console.log('✅ credentials.json 已成功生成！');
  console.log('');
  console.log('輸出位置:', OUTPUT_PATH);
  console.log('');
  console.log('憑證資訊：');
  console.log('- Account ID:', credentials.AccountTag);
  console.log('- Tunnel ID:', credentials.TunnelID);
  console.log('- Secret:', '[REDACTED]');
  console.log('');
  console.log('下一步：');
  console.log('1. 回到上層目錄: cd ..');
  console.log('2. 啟動網關服務: docker-compose up -d');
  console.log('');
  console.log('注意：');
  console.log('- config.yml 會自動從 credentials.json 讀取 Tunnel ID');
  console.log('- 無需手動編輯配置文件');
}

/**
 * 主程式
 */
function main() {
  console.log('🔧 Cloudflare Tunnel Credentials Generator');
  console.log('==========================================');
  console.log('');

  // 檢查是否已存在
  if (checkExisting()) {
    process.exit(0);
  }

  // 讀取 Token
  const token = getToken();

  // 解碼 Token
  const tokenData = decodeToken(token);

  // 提取欄位
  const { accountId, tunnelId, secret } = extractFields(tokenData);

  // 生成 credentials
  const credentials = generateCredentials(accountId, tunnelId, secret);

  // 寫入檔案
  writeCredentials(credentials);
}

// 執行
main();
