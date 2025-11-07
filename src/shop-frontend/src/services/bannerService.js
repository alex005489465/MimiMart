import apiClient from './api'

/**
 * Banner 服務 - 處理輪播圖相關的 API 請求
 */
const bannerService = {
  /**
   * 查詢啟用且已上架的輪播圖
   * @returns {Promise<Array>} Banner 列表
   */
  async getActiveBanners() {
    try {
      const response = await apiClient.get('/api/shop/banner/list')

      if (response.success && Array.isArray(response.data)) {
        return response.data
      }

      console.warn('Banner API 回應格式異常:', response)
      return []
    } catch (error) {
      console.error('查詢輪播圖失敗:', error)
      // 失敗時返回空陣列,避免頁面崩潰
      return []
    }
  }
}

export default bannerService
