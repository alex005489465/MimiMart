import apiClient from './api'

/**
 * 分類管理服務層
 * 封裝後台分類相關 API 呼叫
 */
const categoryService = {
  /**
   * 查詢所有分類
   * @returns {Promise<Array>} 分類列表
   */
  async getAll() {
    try {
      const response = await apiClient.get('/api/admin/category/list')
      return response.data || []
    } catch (error) {
      console.error('查詢分類列表失敗:', error)
      throw error
    }
  },

  /**
   * 查詢單一分類詳情
   * @param {number} categoryId - 分類 ID
   * @returns {Promise<Object>} 分類詳細資料
   */
  async getDetail(categoryId) {
    try {
      const response = await apiClient.get('/api/admin/category/detail', {
        params: { categoryId }
      })
      return response.data
    } catch (error) {
      console.error('查詢分類詳情失敗:', error)
      throw error
    }
  },

  /**
   * 建立新分類
   * @param {Object} data - 分類資料
   * @param {string} data.name - 分類名稱
   * @param {string} [data.description] - 分類描述 (可選)
   * @returns {Promise<Object>} 建立的分類資料
   */
  async create(data) {
    try {
      const response = await apiClient.post('/api/admin/category/create', data)
      return response.data
    } catch (error) {
      console.error('建立分類失敗:', error)
      throw error
    }
  },

  /**
   * 更新分類資訊
   * @param {Object} data - 分類資料
   * @param {number} data.categoryId - 分類 ID
   * @param {string} data.name - 分類名稱
   * @param {string} [data.description] - 分類描述
   * @returns {Promise<Object>} 更新後的分類資料
   */
  async update(data) {
    try {
      const response = await apiClient.post('/api/admin/category/update', data)
      return response.data
    } catch (error) {
      console.error('更新分類失敗:', error)
      throw error
    }
  },

  /**
   * 刪除分類
   * @param {number} categoryId - 分類 ID
   * @returns {Promise<Object>} 刪除結果
   */
  async delete(categoryId) {
    try {
      const response = await apiClient.post('/api/admin/category/delete', {
        categoryId
      })
      return response
    } catch (error) {
      console.error('刪除分類失敗:', error)
      throw error
    }
  }
}

export default categoryService
