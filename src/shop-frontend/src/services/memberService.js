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
    return await apiClient.get('/api/shop/member/profile');
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
    return await apiClient.post('/api/shop/member/profile/update', data);
  },

  /**
   * 修改密碼
   * @param {Object} data - 密碼資料
   * @param {string} data.oldPassword - 舊密碼
   * @param {string} data.newPassword - 新密碼
   * @returns {Promise} 修改結果
   */
  async changePassword(data) {
    return await apiClient.post('/api/shop/member/change-password', data);
  },

  /**
   * 上傳頭像
   * @param {File} file - 圖片檔案
   * @returns {Promise} 上傳結果 (包含 avatarUrl 和 avatarUpdatedAt)
   */
  async uploadAvatar(file) {
    const formData = new FormData();
    formData.append('avatar', file);

    return await apiClient.post('/api/shop/member/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * 取得頭像 URL
   * @param {number} memberId - 會員 ID
   * @returns {string} 頭像的 URL
   */
  getAvatarUrl(memberId) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083';
    return `${baseUrl}/api/shop/member/avatar?memberId=${memberId}`;
  },
};
