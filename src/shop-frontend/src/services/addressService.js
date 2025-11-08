/**
 * 收貨地址相關 API 服務
 */
import apiClient from './api';

export const addressService = {
  /**
   * 查詢收貨地址列表
   * @returns {Promise} 地址列表
   */
  async getAddressList() {
    return await apiClient.get('/api/shop/address/list');
  },

  /**
   * 新增收貨地址
   * @param {Object} data - 地址資料
   * @param {string} data.recipientName - 收件人姓名
   * @param {string} data.phone - 收件人電話
   * @param {string} data.address - 收貨地址
   * @param {boolean} data.isDefault - 是否為預設地址
   * @returns {Promise} 新增結果
   */
  async createAddress(data) {
    return await apiClient.post('/api/shop/address/create', data);
  },

  /**
   * 更新收貨地址
   * @param {Object} data - 地址資料
   * @param {number} data.addressId - 地址 ID
   * @param {string} data.recipientName - 收件人姓名
   * @param {string} data.phone - 收件人電話
   * @param {string} data.address - 收貨地址
   * @param {boolean} data.isDefault - 是否為預設地址
   * @returns {Promise} 更新結果
   */
  async updateAddress(data) {
    return await apiClient.post('/api/shop/address/update', data);
  },

  /**
   * 刪除收貨地址
   * @param {number} addressId - 地址 ID
   * @returns {Promise} 刪除結果
   */
  async deleteAddress(addressId) {
    return await apiClient.post('/api/shop/address/delete', { addressId });
  },

  /**
   * 設為預設地址
   * @param {number} addressId - 地址 ID
   * @returns {Promise} 設定結果
   */
  async setDefaultAddress(addressId) {
    return await apiClient.post('/api/shop/address/set-default', { addressId });
  },
};
