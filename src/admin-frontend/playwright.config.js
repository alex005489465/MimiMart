import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright 測試配置 - MimiMart 管理後台
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './tests',

  // 測試超時設定
  timeout: 30 * 1000,
  expect: {
    timeout: 5000,
  },

  // 並行測試配置
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,

  // 測試報告配置
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list'],
  ],

  // 共用設定
  use: {
    // 基礎 URL（admin-frontend 使用端口 5174）
    baseURL: process.env.VITE_APP_URL || 'http://localhost:5174',

    // 追蹤設定（失敗時保留）
    trace: 'on-first-retry',

    // 截圖設定
    screenshot: 'only-on-failure',

    // 影片設定
    video: 'retain-on-failure',

    // API 基礎 URL
    extraHTTPHeaders: {
      'Accept': 'application/json',
    },
  },

  // 測試專案配置
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    // 可選：其他瀏覽器
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
  ],

  // 注意：開發伺服器在 Docker 容器中運行
  // 測試在本機執行，連接到 http://localhost:5174
  // 因此不需要自動啟動 webServer
});
