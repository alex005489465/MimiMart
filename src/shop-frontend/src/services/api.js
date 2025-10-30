/**
 * Axios 實例配置與請求/回應攔截器
 */
import axios from 'axios';
import { storage } from '../utils/storage';

// 從環境變數取得 API Base URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083';

// 建立 axios 實例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 請求攔截器 - 自動加入 Authorization Header
apiClient.interceptors.request.use(
  (config) => {
    const token = storage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 回應攔截器 - 統一處理錯誤
apiClient.interceptors.response.use(
  (response) => {
    return response.data; // 直接回傳 data 部分
  },
  async (error) => {
    const originalRequest = error.config;

    // 處理 401 錯誤 (Token 過期)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // 嘗試使用 Refresh Token 更新 Access Token
        const refreshToken = storage.getRefreshToken();
        if (refreshToken) {
          const response = await axios.post(
            `${API_BASE_URL}/api/storefront/auth/refresh-token`,
            { refreshToken }
          );

          if (response.data.success) {
            const newAccessToken = response.data.data;
            storage.setAccessToken(newAccessToken);

            // 更新原請求的 Authorization Header
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

            // 重試原請求
            return apiClient(originalRequest);
          }
        }
      } catch (refreshError) {
        // Refresh Token 也失效,清除認證資料並導向登入頁
        storage.clearAuth();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // 處理其他錯誤
    const errorMessage = error.response?.data?.message || '發生錯誤,請稍後再試';
    return Promise.reject({
      message: errorMessage,
      status: error.response?.status,
      data: error.response?.data,
    });
  }
);

export default apiClient;
