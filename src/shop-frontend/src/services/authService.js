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
    return await apiClient.post('/api/shop/auth/register', data);
  },

  /**
   * 會員登入
   * @param {Object} credentials - 登入憑證
   * @param {string} credentials.email - Email
   * @param {string} credentials.password - 密碼
   * @returns {Promise} 登入結果 (包含 accessToken, refreshToken, profile)
   */
  async login(credentials) {
    return await apiClient.post('/api/shop/auth/login', credentials);
  },

  /**
   * 更新 Access Token
   * @param {string} refreshToken - Refresh Token
   * @returns {Promise} 新的 Access Token
   */
  async refreshToken(refreshToken) {
    return await apiClient.post('/api/shop/auth/refresh-token', {
      refreshToken,
    });
  },

  /**
   * 會員登出
   * @returns {Promise} 登出結果
   */
  async logout() {
    return await apiClient.post('/api/shop/auth/logout', {});
  },

  /**
   * 驗證 Email
   * @param {string} token - 驗證 Token
   * @returns {Promise} 驗證結果
   */
  async verifyEmail(token) {
    return await apiClient.post('/api/shop/auth/verify-email', { token });
  },

  /**
   * 重新發送驗證郵件
   * @param {string} email - Email 地址
   * @returns {Promise} 發送結果
   */
  async resendVerificationEmail(email) {
    return await apiClient.post('/api/shop/auth/resend-verification', { email });
  },

  /**
   * 申請密碼重設
   * @param {string} email - Email 地址
   * @returns {Promise} 申請結果
   */
  async forgotPassword(email) {
    return await apiClient.post('/api/shop/auth/forgot-password', { email });
  },

  /**
   * 重設密碼
   * @param {string} token - 重設 Token
   * @param {string} newPassword - 新密碼
   * @param {string} confirmPassword - 確認密碼
   * @returns {Promise} 重設結果
   */
  async resetPassword(token, newPassword, confirmPassword) {
    return await apiClient.post('/api/shop/auth/reset-password', {
      token,
      newPassword,
      confirmPassword,
    });
  },
};
