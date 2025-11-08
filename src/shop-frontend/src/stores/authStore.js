/**
 * 認證狀態管理 Store
 * 取代原本的 AuthContext
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authService } from '../services/authService';
import { memberService } from '../services/memberService';

const useAuthStore = create(
  persist(
    (set, get) => ({
      // 狀態
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: true,

      // 初始化：驗證 token 有效性
      initialize: async () => {
        set({ isLoading: true });
        const token = localStorage.getItem('token');

        if (!token) {
          set({ isLoading: false, isAuthenticated: false, user: null });
          return;
        }

        try {
          const response = await memberService.getProfile();
          set({
            user: response.data,
            token,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (error) {
          // Token 無效，清除本地存儲
          localStorage.removeItem('token');
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
          });
        }
      },

      // 登入
      login: async (credentials) => {
        try {
          const response = await authService.login(credentials);
          const { accessToken, profile } = response.data;

          localStorage.setItem('token', accessToken);
          set({
            user: profile,
            token: accessToken,
            isAuthenticated: true,
          });

          // 登入成功後合併購物車
          const { default: useCartStore } = await import('./cartStore');
          await useCartStore.getState().mergeCart();

          return { success: true };
        } catch (error) {
          return {
            success: false,
            error: error.response?.data?.message || '登入失敗，請稍後再試',
          };
        }
      },

      // 註冊
      register: async (userData) => {
        try {
          // 後端註冊 API 需要 name 而不是 username
          const registerData = {
            email: userData.email,
            password: userData.password,
            name: userData.username, // 將 username 對應到 name
          };
          const response = await authService.register(registerData);
          const { accessToken, profile } = response.data;

          localStorage.setItem('token', accessToken);
          set({
            user: profile,
            token: accessToken,
            isAuthenticated: true,
          });

          return { success: true };
        } catch (error) {
          return {
            success: false,
            error: error.response?.data?.message || '註冊失敗，請稍後再試',
          };
        }
      },

      // 登出
      logout: async () => {
        try {
          // 呼叫後端登出 API
          await authService.logout();
        } catch (error) {
          // 即使後端登出失敗也要清除前端狀態
          console.error('登出 API 呼叫失敗:', error);
        }

        localStorage.removeItem('token');
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        });

        // 登出時清空購物車
        const { default: useCartStore } = await import('./cartStore');
        useCartStore.getState().clearGuestCart();
      },

      // 更新使用者資料
      updateUser: (userData) => {
        set((state) => ({
          user: { ...state.user, ...userData },
        }));
      },

      // 重新整理使用者資料
      refreshUser: async () => {
        try {
          const response = await memberService.getProfile();
          set({ user: response.data });
          return { success: true };
        } catch (error) {
          return {
            success: false,
            error: error.response?.data?.message || '無法取得使用者資料',
          };
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        user: state.user,
      }),
    }
  )
);

export default useAuthStore;
