/**
 * 測試帳號管理輔助工具
 *
 * 提供從後端測試 API 獲取測試帳號的功能
 *
 * 測試帳號規格:
 * - Email: test-member-001@test.com ~ test-member-100@test.com
 * - Password: password123 (所有帳號統一)
 */

const API_BASE_URL = process.env.VITE_API_BASE_URL || 'http://localhost:8083';

/**
 * 從後端測試 API 獲取測試會員帳號
 *
 * @param {number} count - 獲取的帳號數量 (1-100),預設 1
 * @returns {Promise<Array<{email: string, password: string}>>}
 */
export async function getTestMemberAccounts(count = 1) {
  const response = await fetch(`${API_BASE_URL}/api/test/accounts/member?count=${count}`);

  if (!response.ok) {
    throw new Error(`獲取測試帳號失敗: ${response.status} ${response.statusText}`);
  }

  const result = await response.json();

  if (!result.success) {
    throw new Error(`獲取測試帳號失敗: ${result.message}`);
  }

  return result.data;
}

/**
 * 從後端測試 API 獲取單個測試會員帳號
 *
 * @returns {Promise<{email: string, password: string}>}
 */
export async function getTestMemberAccount() {
  const accounts = await getTestMemberAccounts(1);
  return accounts[0];
}


/**
 * 使用測試帳號登入
 *
 * @param {Object} page - Playwright Page 物件
 * @param {string} email - Email
 * @param {string} password - 密碼
 */
export async function loginWithTestAccount(page, email, password) {
  // 導航到登入頁面
  await page.goto('/login');

  // 等待登入表單載入
  await page.waitForSelector('input[type="email"]');

  // 填寫登入表單
  await page.fill('input[type="email"]', email);
  await page.fill('input[type="password"]', password);

  // 點擊登入按鈕
  await page.click('button[type="submit"]');

  // 等待登入成功 (可能會跳轉到首頁或其他頁面)
  await page.waitForURL(url => url.pathname !== '/login', { timeout: 5000 });
}

/**
 * 登出當前會員
 *
 * @param {Object} page - Playwright Page 物件
 */
export async function logoutTestAccount(page) {
  // 先導航到會員頁面 (該頁面側邊欄有登出按鈕)
  await page.goto('/member');

  // 等待頁面載入
  await page.waitForTimeout(500);

  // 找到登出按鈕並點擊
  await page.click('button:has-text("登出")');

  // 等待登出完成
  await page.waitForURL(url => url.pathname === '/login' || url.pathname === '/', { timeout: 5000 });
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
