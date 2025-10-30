/**
 * 認證相關 API 服務
 */
import apiClient from './api';

export const authService = {
  /**
   * 會員註冊
   * @param {Object} data - 註冊資料
   * @param {string} data.email - Email
   * @param {string} data.password - 密碼
   * @param {string} data.name - 姓名
   * @returns {Promise} 註冊結果
   */
  async register(data) {
    return await apiClient.post('/api/storefront/auth/register', data);
  },

  /**
   * 會員登入
   * @param {Object} credentials - 登入憑證
   * @param {string} credentials.email - Email
   * @param {string} credentials.password - 密碼
   * @returns {Promise} 登入結果 (包含 accessToken, refreshToken, profile)
   */
  async login(credentials) {
    return await apiClient.post('/api/storefront/auth/login', credentials);
  },

  /**
   * 更新 Access Token
   * @param {string} refreshToken - Refresh Token
   * @returns {Promise} 新的 Access Token
   */
  async refreshToken(refreshToken) {
    return await apiClient.post('/api/storefront/auth/refresh-token', {
      refreshToken,
    });
  },
};
