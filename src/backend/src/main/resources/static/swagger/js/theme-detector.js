/**
 * Swagger UI 主題自動偵測器
 * 根據系統的 prefers-color-scheme 自動切換明暗模式
 */
(function() {
    'use strict';

    const DARK_THEME_CSS_PATH = '/swagger/css/swagger-dark-theme.css';
    const DARK_THEME_LINK_ID = 'swagger-dark-theme-stylesheet';

    /**
     * 檢查系統是否偏好暗色模式
     */
    function prefersDarkMode() {
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    }

    /**
     * 載入暗色主題 CSS
     */
    function loadDarkTheme() {
        // 檢查是否已經載入過
        if (document.getElementById(DARK_THEME_LINK_ID)) {
            console.log('[Swagger Theme] 暗色主題已經載入');
            return;
        }

        const link = document.createElement('link');
        link.id = DARK_THEME_LINK_ID;
        link.rel = 'stylesheet';
        link.type = 'text/css';
        link.href = DARK_THEME_CSS_PATH;

        // 載入完成後的回調
        link.onload = function() {
            console.log('[Swagger Theme] ✓ 暗色主題已套用');
        };

        link.onerror = function() {
            console.error('[Swagger Theme] ✗ 暗色主題載入失敗:', DARK_THEME_CSS_PATH);
        };

        document.head.appendChild(link);
    }

    /**
     * 移除暗色主題 CSS
     */
    function removeDarkTheme() {
        const existingLink = document.getElementById(DARK_THEME_LINK_ID);
        if (existingLink) {
            existingLink.remove();
            console.log('[Swagger Theme] ✓ 已切換回亮色主題');
        }
    }

    /**
     * 根據系統偏好套用主題
     */
    function applyThemeBasedOnSystemPreference() {
        if (prefersDarkMode()) {
            console.log('[Swagger Theme] 偵測到系統偏好: 暗色模式');
            loadDarkTheme();
        } else {
            console.log('[Swagger Theme] 偵測到系統偏好: 亮色模式');
            removeDarkTheme();
        }
    }

    /**
     * 監聽系統主題變更
     */
    function watchSystemThemeChanges() {
        if (window.matchMedia) {
            const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

            // 監聽變更事件
            // 使用 addEventListener (現代瀏覽器) 或 addListener (舊版瀏覽器)
            if (mediaQuery.addEventListener) {
                mediaQuery.addEventListener('change', function(e) {
                    console.log('[Swagger Theme] 系統主題已變更:', e.matches ? '暗色' : '亮色');
                    applyThemeBasedOnSystemPreference();
                });
            } else if (mediaQuery.addListener) {
                // 支援舊版瀏覽器
                mediaQuery.addListener(function() {
                    console.log('[Swagger Theme] 系統主題已變更');
                    applyThemeBasedOnSystemPreference();
                });
            }

            console.log('[Swagger Theme] ✓ 已啟用系統主題變更監聽');
        } else {
            console.warn('[Swagger Theme] ⚠ 瀏覽器不支援 prefers-color-scheme');
        }
    }

    /**
     * 初始化主題偵測器
     */
    function initialize() {
        console.log('[Swagger Theme] 正在初始化主題偵測器...');

        // 立即套用主題
        applyThemeBasedOnSystemPreference();

        // 監聽系統主題變更
        watchSystemThemeChanges();

        console.log('[Swagger Theme] ✓ 主題偵測器初始化完成');
    }

    // 等待 DOM 載入完成後初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        // DOM 已經載入完成
        initialize();
    }

})();
