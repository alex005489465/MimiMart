import apiClient from './api'

/**
 * Banner 管理服務層
 * 封裝後台 Banner 相關 API 呼叫
 */
const bannerService = {
  /**
   * 查詢所有 Banner (含停用)
   * @returns {Promise<Array>} Banner 列表
   */
  async getAll() {
    try {
      const response = await apiClient.get('/api/admin/banner/list')
      return response.data || []
    } catch (error) {
      console.error('查詢 Banner 列表失敗:', error)
      throw error
    }
  },

  /**
   * 查詢單一 Banner 詳情
   * @param {number} bannerId - Banner ID
   * @returns {Promise<Object>} Banner 詳細資料
   */
  async getDetail(bannerId) {
    try {
      const response = await apiClient.get('/api/admin/banner/detail', {
        params: { bannerId }
      })
      return response.data
    } catch (error) {
      console.error('查詢 Banner 詳情失敗:', error)
      throw error
    }
  },

  /**
   * 建立新 Banner (含圖片上傳)
   * @param {Object} data - Banner 資料
   * @param {string} data.title - 標題
   * @param {File} data.imageFile - 圖片檔案
   * @param {string} [data.linkUrl] - 點擊連結 (可選)
   * @param {number} data.displayOrder - 顯示順序
   * @param {string} [data.publishedAt] - 上架時間 (可選，NULL 表示立即上架)
   * @param {string} [data.unpublishedAt] - 下架時間 (可選，NULL 表示永不下架)
   * @returns {Promise<Object>} 建立的 Banner 資料
   */
  async create(data) {
    try {
      const formData = new FormData()
      formData.append('title', data.title)
      formData.append('imageFile', data.imageFile)
      if (data.linkUrl) {
        formData.append('linkUrl', data.linkUrl)
      }
      formData.append('displayOrder', data.displayOrder)
      if (data.publishedAt) {
        formData.append('publishedAt', data.publishedAt)
      }
      if (data.unpublishedAt) {
        formData.append('unpublishedAt', data.unpublishedAt)
      }

      const response = await apiClient.post('/api/admin/banner/create', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      return response.data
    } catch (error) {
      console.error('建立 Banner 失敗:', error)
      throw error
    }
  },

  /**
   * 更新 Banner 資訊 (不更新圖片)
   * @param {Object} data - Banner 資料
   * @param {number} data.bannerId - Banner ID
   * @param {string} data.title - 標題
   * @param {string} [data.linkUrl] - 點擊連結
   * @param {number} data.displayOrder - 顯示順序
   * @param {string} [data.publishedAt] - 上架時間 (可選，NULL 表示立即上架)
   * @param {string} [data.unpublishedAt] - 下架時間 (可選，NULL 表示永不下架)
   * @returns {Promise<Object>} 更新後的 Banner 資料
   */
  async update(data) {
    try {
      const response = await apiClient.post('/api/admin/banner/update', data)
      return response.data
    } catch (error) {
      console.error('更新 Banner 失敗:', error)
      throw error
    }
  },

  /**
   * 更新 Banner 並替換圖片
   * @param {Object} data - Banner 資料
   * @param {number} data.bannerId - Banner ID
   * @param {File} data.imageFile - 新圖片檔案
   * @param {string} [data.title] - 新標題 (可選)
   * @param {string} [data.linkUrl] - 新連結 (可選)
   * @param {number} [data.displayOrder] - 新顯示順序 (可選)
   * @param {string} [data.publishedAt] - 上架時間 (可選，NULL 表示立即上架)
   * @param {string} [data.unpublishedAt] - 下架時間 (可選，NULL 表示永不下架)
   * @returns {Promise<Object>} 更新後的 Banner 資料
   */
  async updateWithImage(data) {
    try {
      const formData = new FormData()
      formData.append('bannerId', data.bannerId)
      formData.append('imageFile', data.imageFile)
      if (data.title) {
        formData.append('title', data.title)
      }
      if (data.linkUrl) {
        formData.append('linkUrl', data.linkUrl)
      }
      if (data.displayOrder !== undefined) {
        formData.append('displayOrder', data.displayOrder)
      }
      if (data.publishedAt) {
        formData.append('publishedAt', data.publishedAt)
      }
      if (data.unpublishedAt) {
        formData.append('unpublishedAt', data.unpublishedAt)
      }

      const response = await apiClient.post('/api/admin/banner/update-with-image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      return response.data
    } catch (error) {
      console.error('更新 Banner 並替換圖片失敗:', error)
      throw error
    }
  },

  /**
   * 刪除 Banner
   * @param {number} bannerId - Banner ID
   * @returns {Promise<Object>} 刪除結果
   */
  async delete(bannerId) {
    try {
      const response = await apiClient.post('/api/admin/banner/delete', {
        bannerId
      })
      return response
    } catch (error) {
      console.error('刪除 Banner 失敗:', error)
      throw error
    }
  },

  /**
   * 啟用 Banner
   * @param {number} bannerId - Banner ID
   * @returns {Promise<Object>} 啟用後的 Banner 資料
   */
  async activate(bannerId) {
    try {
      const response = await apiClient.post('/api/admin/banner/activate', {
        bannerId
      })
      return response.data
    } catch (error) {
      console.error('啟用 Banner 失敗:', error)
      throw error
    }
  },

  /**
   * 停用 Banner
   * @param {number} bannerId - Banner ID
   * @returns {Promise<Object>} 停用後的 Banner 資料
   */
  async deactivate(bannerId) {
    try {
      const response = await apiClient.post('/api/admin/banner/deactivate', {
        bannerId
      })
      return response.data
    } catch (error) {
      console.error('停用 Banner 失敗:', error)
      throw error
    }
  },

  /**
   * 更新 Banner 顯示順序
   * @param {number} bannerId - Banner ID
   * @param {number} displayOrder - 新的顯示順序
   * @returns {Promise<Object>} 更新後的 Banner 資料
   */
  async updateOrder(bannerId, displayOrder) {
    try {
      const response = await apiClient.post('/api/admin/banner/update-order', {
        bannerId,
        displayOrder
      })
      return response.data
    } catch (error) {
      console.error('更新 Banner 順序失敗:', error)
      throw error
    }
  }
}

export default bannerService
