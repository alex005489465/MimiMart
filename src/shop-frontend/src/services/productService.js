import api from './api';

/**
 * 商品相關 API 服務
 *
 * 注意: api.js 的回應攔截器已經解包 response.data
 * 所以這裡直接使用 response,不需要再 .data
 */
const productService = {
  /**
   * 搜尋商品
   * @param {string} keyword - 搜尋關鍵字
   * @param {number} page - 頁碼 (1-based)
   * @param {number} size - 每頁筆數
   * @returns {Promise<Object>} 搜尋結果與分頁資訊
   */
  searchProducts: async (keyword, page = 1, size = 20) => {
    try {
      const response = await api.get('/api/shop/product/search', {
        params: { keyword, page, size }
      });
      return response; // api.js 已經解包過了
    } catch (error) {
      console.error('搜尋商品失敗:', error);
      throw error;
    }
  },

  /**
   * 取得商品列表
   * @param {Object} params - 查詢參數
   * @param {number} params.categoryId - 分類 ID (可選)
   * @param {number} params.page - 頁碼 (1-based)
   * @param {number} params.size - 每頁筆數
   * @param {string} params.sortBy - 排序欄位 (createdAt, price, name)
   * @param {string} params.sortDir - 排序方向 (ASC, DESC)
   * @returns {Promise<Object>} 商品列表與分頁資訊
   */
  getProductList: async ({ categoryId, page = 1, size = 20, sortBy = 'createdAt', sortDir = 'DESC' } = {}) => {
    try {
      const params = { page, size, sortBy, sortDir };
      if (categoryId) {
        params.categoryId = categoryId;
      }

      const response = await api.get('/api/shop/product/list', { params });
      return response; // api.js 已經解包過了
    } catch (error) {
      console.error('取得商品列表失敗:', error);
      throw error;
    }
  },

  /**
   * 取得商品詳情
   * @param {number} productId - 商品 ID
   * @returns {Promise<Object>} 商品詳細資訊
   */
  getProductDetail: async (productId) => {
    try {
      const response = await api.get('/api/shop/product/detail', {
        params: { productId }
      });
      return response; // api.js 已經解包過了
    } catch (error) {
      console.error('取得商品詳情失敗:', error);
      throw error;
    }
  }
};

export default productService;
