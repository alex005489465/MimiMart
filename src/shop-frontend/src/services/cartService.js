import api from './api';

/**
 * 購物車相關 API 服務
 *
 * 注意: api.js 的回應攔截器已經解包 response.data
 * 所以這裡直接使用 response,不需要再 .data
 */
const cartService = {
  /**
   * 查詢購物車
   * @returns {Promise<Object>} CartSummaryDTO - 購物車摘要資訊
   * @property {Array} items - 購物車項目列表
   * @property {number} totalItems - 商品種類數
   * @property {number} totalQuantity - 總件數
   * @property {number} totalAmount - 總金額
   */
  getCart: async () => {
    try {
      const response = await api.get('/api/shop/cart');
      return response;
    } catch (error) {
      console.error('查詢購物車失敗:', error);
      throw error;
    }
  },

  /**
   * 加入商品至購物車
   * @param {number} productId - 商品 ID
   * @param {number} quantity - 數量
   * @returns {Promise<Object>} CartItemDTO - 購物車項目資訊
   */
  addToCart: async (productId, quantity) => {
    try {
      const response = await api.post('/api/shop/cart/add', {
        productId,
        quantity
      });
      return response;
    } catch (error) {
      console.error('加入購物車失敗:', error);
      throw error;
    }
  },

  /**
   * 更新購物車項目數量
   * @param {number} productId - 商品 ID
   * @param {number} quantity - 新數量
   * @returns {Promise<Object>} CartItemDTO - 更新後的購物車項目資訊
   */
  updateCartItem: async (productId, quantity) => {
    try {
      const response = await api.post('/api/shop/cart/item/update', {
        productId,
        quantity
      });
      return response;
    } catch (error) {
      console.error('更新購物車項目失敗:', error);
      throw error;
    }
  },

  /**
   * 移除購物車項目
   * @param {number} productId - 商品 ID
   * @returns {Promise<void>}
   */
  removeCartItem: async (productId) => {
    try {
      const response = await api.post('/api/shop/cart/item/remove', {
        productId
      });
      return response;
    } catch (error) {
      console.error('移除購物車項目失敗:', error);
      throw error;
    }
  },

  /**
   * 清空購物車
   * @returns {Promise<void>}
   */
  clearCart: async () => {
    try {
      const response = await api.post('/api/shop/cart/clear');
      return response;
    } catch (error) {
      console.error('清空購物車失敗:', error);
      throw error;
    }
  },

  /**
   * 合併購物車 (登入時使用)
   * @param {Array} items - 訪客購物車項目列表
   * @param {number} items[].productId - 商品 ID
   * @param {number} items[].quantity - 數量
   * @returns {Promise<Object>} CartSummaryDTO - 合併後的購物車摘要資訊
   */
  mergeCart: async (items) => {
    try {
      const response = await api.post('/api/shop/cart/merge', {
        items
      });
      return response;
    } catch (error) {
      console.error('合併購物車失敗:', error);
      throw error;
    }
  }
};

export default cartService;
