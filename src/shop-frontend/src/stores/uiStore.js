/**
 * UI 狀態管理 Store
 * 管理主題、載入狀態、通知等 UI 相關狀態
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useUIStore = create(
  persist(
    (set, get) => ({
      // 主題模式
      theme: 'light', // 'light' | 'dark'

      // 全域載入狀態
      isGlobalLoading: false,

      // 側邊欄狀態（行動版）
      isSidebarOpen: false,

      // 切換主題
      toggleTheme: () => {
        set((state) => ({
          theme: state.theme === 'light' ? 'dark' : 'light',
        }));
      },

      // 設定主題
      setTheme: (theme) => {
        if (theme === 'light' || theme === 'dark') {
          set({ theme });
        }
      },

      // 設定全域載入狀態
      setGlobalLoading: (isLoading) => {
        set({ isGlobalLoading: isLoading });
      },

      // 切換側邊欄
      toggleSidebar: () => {
        set((state) => ({
          isSidebarOpen: !state.isSidebarOpen,
        }));
      },

      // 開啟側邊欄
      openSidebar: () => {
        set({ isSidebarOpen: true });
      },

      // 關閉側邊欄
      closeSidebar: () => {
        set({ isSidebarOpen: false });
      },
    }),
    {
      name: 'ui-storage',
      partialize: (state) => ({
        theme: state.theme,
      }),
    }
  )
);

export default useUIStore;
