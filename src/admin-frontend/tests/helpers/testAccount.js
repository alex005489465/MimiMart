/**
 * 測試帳號管理輔助工具 - 管理員版本
 *
 * 提供從後端測試 API 獲取管理員測試帳號的功能
 *
 * 測試帳號規格:
 * - Username: test-admin-001 ~ test-admin-100
 * - Email: test-admin-001@test.com ~ test-admin-100@test.com
 * - Password: admin123 (所有帳號統一)
 */

const API_BASE_URL = process.env.VITE_API_BASE_URL || 'http://localhost:8083';

/**
 * 從後端測試 API 獲取管理員測試帳號
 *
 * @param {number} count - 獲取的帳號數量 (1-100),預設 1
 * @returns {Promise<Array<{username: string, email: string, password: string}>>}
 */
export async function getTestAdminAccounts(count = 1) {
  const response = await fetch(`${API_BASE_URL}/api/test/accounts/admin?count=${count}`);

  if (!response.ok) {
    throw new Error(`獲取測試管理員帳號失敗: ${response.status} ${response.statusText}`);
  }

  const result = await response.json();

  if (!result.success) {
    throw new Error(`獲取測試管理員帳號失敗: ${result.message}`);
  }

  return result.data;
}

/**
 * 從後端測試 API 獲取單個管理員測試帳號
 *
 * @returns {Promise<{username: string, email: string, password: string}>}
 */
export async function getTestAdminAccount() {
  const accounts = await getTestAdminAccounts(1);
  return accounts[0];
}


/**
 * 使用測試帳號登入管理後台
 *
 * @param {Object} page - Playwright Page 物件
 * @param {string} username - 管理員帳號
 * @param {string} password - 密碼
 */
export async function loginWithTestAccount(page, username, password) {
  // 導航到登入頁面
  await page.goto('/');

  // 等待登入表單載入
  await page.waitForSelector('input[name="username"]');

  // 填寫登入表單
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);

  // 點擊登入按鈕
  await page.click('button[type="submit"]');

  // 等待登入成功（應該會跳轉到 /dashboard）
  await page.waitForURL(url => url.pathname === '/dashboard', { timeout: 5000 });
}

/**
 * 登出當前管理員
 *
 * @param {Object} page - Playwright Page 物件
 */
export async function logoutTestAccount(page) {
  // 導航到儀表板（該頁面側邊欄有登出按鈕）
  await page.goto('/dashboard');

  // 等待頁面載入
  await page.waitForTimeout(500);

  // 找到登出按鈕並點擊
  await page.click('button:has-text("登出")');

  // 等待登出完成（應該會跳轉到登入頁面）
  await page.waitForURL(url => url.pathname === '/', { timeout: 5000 });
}


/**
 * 檢查測試端點是否可用
 *
 * @returns {Promise<boolean>}
 */
export async function isTestEndpointAvailable() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/test/health`);
    return response.ok;
  } catch (error) {
    return false;
  }
}
