import axios from 'axios'

/**
 * API 基礎 URL
 * 開發環境：使用空字串讓請求走 Vite proxy
 * 生產環境：從環境變數取得實際 API 位址
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

/**
 * 建立 axios 實例
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000, // 60 秒,適應 AI 生成需要的時間
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 請求攔截器 - 自動添加認證 token
 */
apiClient.interceptors.request.use(
  (config) => {
    // TODO: 從 localStorage 或其他地方取得 token
    const token = localStorage.getItem('adminToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 回應攔截器 - 統一處理錯誤
 */
apiClient.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      // 伺服器回應錯誤
      const { status, data } = error.response

      switch (status) {
        case 401:
          // 未授權，清除登入狀態並導向登入頁
          localStorage.removeItem('adminToken')
          localStorage.removeItem('isAuthenticated')
          localStorage.removeItem('adminUser')
          window.location.href = '/'
          break
        case 403:
          console.error('權限不足')
          break
        case 404:
          console.error('資源不存在')
          break
        case 500:
          console.error('伺服器錯誤')
          break
        default:
          console.error('請求失敗:', data?.message || error.message)
      }

      return Promise.reject(data || error)
    } else if (error.request) {
      // 請求已發送但沒有收到回應
      console.error('無法連接到伺服器')
      return Promise.reject(new Error('無法連接到伺服器'))
    } else {
      // 其他錯誤
      console.error('請求設定錯誤:', error.message)
      return Promise.reject(error)
    }
  }
)

export default apiClient
