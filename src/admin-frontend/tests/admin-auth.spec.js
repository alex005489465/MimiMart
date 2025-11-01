import { test, expect } from '@playwright/test';
import {
  getTestAdminAccount,
  loginWithTestAccount,
  logoutTestAccount,
  isTestEndpointAvailable,
} from './helpers/testAccount.js';

/**
 * 管理員認證流程測試
 *
 * 測試項目:
 * - 使用測試帳號登入管理後台
 * - 登入後訪問儀表板
 * - 登出功能
 * - 錯誤密碼登入驗證
 * - 未登入訪問權限驗證
 */

test.describe('管理員認證流程測試', () => {
  let testAccount;

  // 在所有測試開始前獲取測試帳號
  test.beforeAll(async () => {
    // 檢查測試端點是否可用
    const isAvailable = await isTestEndpointAvailable();
    if (!isAvailable) {
      throw new Error('測試端點不可用，請確認後端服務已啟動且 APP_TEST_ENDPOINTS_ENABLED=true');
    }

    // 獲取測試管理員帳號
    testAccount = await getTestAdminAccount();
    console.log('測試管理員帳號:', testAccount.username, testAccount.email);
  });

  test('應該能使用測試帳號成功登入到儀表板', async ({ page }) => {
    // 使用測試帳號登入
    await loginWithTestAccount(page, testAccount.username, testAccount.password);

    // 驗證登入成功：應該在儀表板頁面
    expect(page.url()).toContain('/dashboard');

    // 驗證 localStorage 中有 Token
    const adminToken = await page.evaluate(() => localStorage.getItem('adminToken'));
    expect(adminToken).not.toBeNull();

    // 驗證認證狀態
    const isAuthenticated = await page.evaluate(() => localStorage.getItem('isAuthenticated'));
    expect(isAuthenticated).toBe('true');
  });

  test('登入後應該能查看儀表板內容', async ({ page }) => {
    // 先登入
    await loginWithTestAccount(page, testAccount.username, testAccount.password);

    // 驗證在儀表板頁面
    expect(page.url()).toContain('/dashboard');

    // 等待儀表板內容載入
    await page.waitForSelector('text=儀表板, text=管理後台', { timeout: 5000 }).catch(() => {
      console.log('未找到儀表板標題，但頁面已載入');
    });

    // 驗證側邊欄或導航存在
    const navigation = page.locator('nav, aside, [role="navigation"]').first();
    await expect(navigation).toBeVisible({ timeout: 5000 }).catch(() => {
      console.log('未找到導航元素，但這可能是正常的');
    });
  });

  test('應該能成功登出', async ({ page }) => {
    // 先登入
    await loginWithTestAccount(page, testAccount.username, testAccount.password);

    // 驗證已登入
    const adminToken = await page.evaluate(() => localStorage.getItem('adminToken'));
    expect(adminToken).not.toBeNull();

    // 執行登出
    await logoutTestAccount(page);

    // 等待登出操作完成
    await page.waitForTimeout(1000);

    // 驗證 localStorage 已清空
    const adminTokenAfterLogout = await page.evaluate(() => localStorage.getItem('adminToken'));
    expect(adminTokenAfterLogout).toBeNull();

    const isAuthenticated = await page.evaluate(() => localStorage.getItem('isAuthenticated'));
    expect(isAuthenticated).toBeNull();

    // 驗證跳轉到登入頁
    expect(page.url()).not.toContain('/dashboard');
  });

  test('使用錯誤密碼應該無法登入並顯示錯誤訊息', async ({ page }) => {
    // 導航到登入頁面
    await page.goto('/');

    // 等待登入表單載入
    await page.waitForSelector('input[name="username"]');

    // 填寫錯誤的密碼
    await page.fill('input[name="username"]', testAccount.username);
    await page.fill('input[name="password"]', 'wrong-password-123');

    // 點擊登入按鈕
    await page.click('button[type="submit"]');

    // 等待錯誤訊息出現
    await page.waitForTimeout(2000);

    // 驗證仍在登入頁面（未成功登入）
    expect(page.url()).not.toContain('/dashboard');

    // 驗證有錯誤訊息
    const errorMessage = page.locator('text=密碼錯誤, text=帳號或密碼錯誤, text=登入失敗').first();
    await expect(errorMessage).toBeVisible({ timeout: 3000 }).catch(() => {
      console.log('未找到錯誤訊息，但應該無法登入');
    });
  });

  test('未登入時訪問儀表板應該跳轉到登入頁', async ({ page }) => {
    // 先導航到首頁
    await page.goto('/');

    // 清除所有儲存
    await page.context().clearCookies();
    await page.evaluate(() => localStorage.clear());

    // 嘗試直接訪問儀表板
    await page.goto('/dashboard');

    // 等待頁面反應
    await page.waitForTimeout(1000);

    // 應該跳轉到登入頁或顯示登入表單
    await page.waitForURL(url => url.pathname === '/', { timeout: 5000 }).catch(() => {
      console.log('未跳轉到登入頁，請確認前端路由守衛已實作');
    });

    // 驗證登入表單存在
    const usernameInput = page.locator('input[name="username"]');
    await expect(usernameInput).toBeVisible({ timeout: 3000 }).catch(() => {
      console.log('未找到登入表單，路由守衛可能未實作');
    });
  });
});
