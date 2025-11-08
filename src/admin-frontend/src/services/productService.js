import apiClient from './api'

/**
 * 商品管理服務層
 * 封裝後台商品相關 API 呼叫
 */
const productService = {
  /**
   * 查詢商品列表
   * @param {string} status - 商品狀態 (all, published, unpublished)
   * @param {number} page - 頁碼 (從 1 開始)
   * @param {number} size - 每頁筆數
   * @returns {Promise<Object>} 商品列表及分頁資訊
   */
  async getList(status = 'all', page = 1, size = 20) {
    try {
      const response = await apiClient.get('/api/admin/product/list', {
        params: { status, page, size }
      })
      return response
    } catch (error) {
      console.error('查詢商品列表失敗:', error)
      throw error
    }
  },

  /**
   * 查詢單一商品詳情
   * @param {number} productId - 商品 ID
   * @returns {Promise<Object>} 商品詳細資料
   */
  async getDetail(productId) {
    try {
      const response = await apiClient.get('/api/admin/product/detail', {
        params: { productId }
      })
      return response.data
    } catch (error) {
      console.error('查詢商品詳情失敗:', error)
      throw error
    }
  },

  /**
   * 建立新商品
   * @param {Object} data - 商品資料
   * @param {string} data.name - 商品名稱 (必填，最大 200 字元)
   * @param {string} [data.description] - 商品描述 (可選)
   * @param {number} data.price - 商品售價 (必填，0.01 ~ 99,999,999.99)
   * @param {number} data.stock - 商品庫存 (必填，>= 0)
   * @param {string} [data.imageUrl] - 商品圖片 URL (可選)
   * @param {number} data.categoryId - 分類 ID (必填)
   * @param {string} [data.publishedAt] - 上架時間 (可選，NULL 表示不限制)
   * @param {string} [data.unpublishedAt] - 下架時間 (可選，NULL 表示不限制)
   * @returns {Promise<Object>} 建立的商品資料
   */
  async create(data) {
    try {
      const response = await apiClient.post('/api/admin/product/create', data)
      return response.data
    } catch (error) {
      console.error('建立商品失敗:', error)
      throw error
    }
  },

  /**
   * 更新商品資訊
   * @param {Object} data - 商品資料
   * @param {number} data.productId - 商品 ID (必填)
   * @param {string} data.name - 商品名稱 (必填，最大 200 字元)
   * @param {string} [data.description] - 商品描述
   * @param {number} data.price - 商品售價 (必填，0.01 ~ 99,999,999.99)
   * @param {number} data.stock - 商品庫存 (必填，>= 0)
   * @param {string} [data.imageUrl] - 商品圖片 URL
   * @param {number} data.categoryId - 分類 ID (必填)
   * @param {string} [data.publishedAt] - 上架時間 (可選，NULL 表示不限制)
   * @param {string} [data.unpublishedAt] - 下架時間 (可選，NULL 表示不限制)
   * @returns {Promise<Object>} 更新後的商品資料
   */
  async update(data) {
    try {
      const response = await apiClient.post('/api/admin/product/update', data)
      return response.data
    } catch (error) {
      console.error('更新商品失敗:', error)
      throw error
    }
  },

  /**
   * 刪除商品 (軟刪除)
   * @param {number} productId - 商品 ID
   * @returns {Promise<Object>} 刪除結果
   */
  async delete(productId) {
    try {
      const response = await apiClient.post('/api/admin/product/delete', {
        productId
      })
      return response
    } catch (error) {
      console.error('刪除商品失敗:', error)
      throw error
    }
  },

  /**
   * 上架商品
   * @param {number} productId - 商品 ID
   * @returns {Promise<Object>} 上架後的商品資料
   */
  async publish(productId) {
    try {
      const response = await apiClient.post('/api/admin/product/publish', {
        productId
      })
      return response.data
    } catch (error) {
      console.error('上架商品失敗:', error)
      throw error
    }
  },

  /**
   * 下架商品
   * @param {number} productId - 商品 ID
   * @returns {Promise<Object>} 下架後的商品資料
   */
  async unpublish(productId) {
    try {
      const response = await apiClient.post('/api/admin/product/unpublish', {
        productId
      })
      return response.data
    } catch (error) {
      console.error('下架商品失敗:', error)
      throw error
    }
  },

  /**
   * 啟用商品
   * @param {number} productId - 商品 ID
   * @returns {Promise<Object>} 啟用後的商品資料
   */
  async activate(productId) {
    try {
      const response = await apiClient.post('/api/admin/product/activate', {
        productId
      })
      return response.data
    } catch (error) {
      console.error('啟用商品失敗:', error)
      throw error
    }
  },

  /**
   * 停用商品
   * @param {number} productId - 商品 ID
   * @returns {Promise<Object>} 停用後的商品資料
   */
  async deactivate(productId) {
    try {
      const response = await apiClient.post('/api/admin/product/deactivate', {
        productId
      })
      return response.data
    } catch (error) {
      console.error('停用商品失敗:', error)
      throw error
    }
  },

  /**
   * 上傳商品圖片
   * @param {File} file - 圖片檔案
   * @returns {Promise<Object>} 圖片上傳結果 (包含 imageUrl)
   */
  async uploadImage(file) {
    try {
      const formData = new FormData()
      formData.append('image', file)

      const response = await apiClient.post(
        '/api/admin/product/image/upload',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      )
      return response.data
    } catch (error) {
      console.error('上傳商品圖片失敗:', error)
      throw error
    }
  },

  /**
   * 刪除商品圖片
   * @param {string} imageUrl - 圖片 URL
   * @returns {Promise<Object>} 刪除結果
   */
  async deleteImage(imageUrl) {
    try {
      const response = await apiClient.post('/api/admin/product/image/delete', {
        imageUrl
      })
      return response
    } catch (error) {
      console.error('刪除商品圖片失敗:', error)
      throw error
    }
  }
}

export default productService
