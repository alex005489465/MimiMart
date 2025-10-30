/**
 * 會員資料相關 API 服務
 */
import apiClient from './api';

export const memberService = {
  /**
   * 查看個人資料
   * @returns {Promise} 會員資料
   */
  async getProfile() {
    return await apiClient.get('/api/storefront/member/profile');
  },

  /**
   * 更新個人資料
   * @param {Object} data - 更新資料
   * @param {string} data.name - 姓名
   * @param {string} data.phone - 電話
   * @param {string} data.homeAddress - 地址
   * @returns {Promise} 更新結果
   */
  async updateProfile(data) {
    return await apiClient.post('/api/storefront/member/profile/update', data);
  },

  /**
   * 修改密碼
   * @param {Object} data - 密碼資料
   * @param {string} data.oldPassword - 舊密碼
   * @param {string} data.newPassword - 新密碼
   * @returns {Promise} 修改結果
   */
  async changePassword(data) {
    return await apiClient.post('/api/storefront/member/change-password', data);
  },
};
