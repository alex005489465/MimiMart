import apiClient from './api'

/**
 * 管理員認證服務
 */
const authService = {
  /**
   * 管理員登入
   * @param {Object} credentials - 登入憑證
   * @param {string} credentials.username - 帳號
   * @param {string} credentials.password - 密碼
   * @returns {Promise<Object>} 登入回應（包含 token 和使用者資訊）
   */
  async login(credentials) {
    try {
      const response = await apiClient.post('/api/admin/auth/login', credentials)

      // 後端使用 ApiResponse 包裝，實際資料在 data 欄位中
      const { data } = response

      // 儲存 token 和使用者資訊
      if (data.accessToken) {
        localStorage.setItem('adminToken', data.accessToken)
        localStorage.setItem('isAuthenticated', 'true')
        localStorage.setItem('adminUser', JSON.stringify(data.profile || { username: credentials.username }))

        // 選擇性儲存 refreshToken
        if (data.refreshToken) {
          localStorage.setItem('adminRefreshToken', data.refreshToken)
        }
      }

      return data
    } catch (error) {
      throw error
    }
  },

  /**
   * 管理員登出
   */
  logout() {
    localStorage.removeItem('adminToken')
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('adminUser')
    localStorage.removeItem('adminRefreshToken')
  },

  /**
   * 檢查是否已登入
   * @returns {boolean}
   */
  isAuthenticated() {
    return localStorage.getItem('isAuthenticated') === 'true'
  },

  /**
   * 取得當前管理員資訊
   * @returns {Object|null}
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('adminUser')
    if (!userStr) return null

    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  },

  /**
   * 取得管理員 token
   * @returns {string|null}
   */
  getToken() {
    return localStorage.getItem('adminToken')
  }
}

export default authService
