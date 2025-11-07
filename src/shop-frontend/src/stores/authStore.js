/**
 * 認證狀態管理 Store
 * 取代原本的 AuthContext
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import api from '../services/api';

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
          const response = await api.get('/api/users/profile');
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
          const response = await api.post('/api/users/login', credentials);
          const { token, user } = response.data;

          localStorage.setItem('token', token);
          set({
            user,
            token,
            isAuthenticated: true,
          });

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
          const response = await api.post('/api/users/register', userData);
          const { token, user } = response.data;

          localStorage.setItem('token', token);
          set({
            user,
            token,
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
      logout: () => {
        localStorage.removeItem('token');
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        });
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
          const response = await api.get('/api/users/profile');
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
