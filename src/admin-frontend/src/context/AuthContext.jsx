import { createContext, useContext, useState, useEffect } from 'react'
import authService from '@/services/authService'
import apiClient from '@/services/api'

/**
 * 認證 Context
 */
const AuthContext = createContext(null)

/**
 * 認證 Provider 元件
 * 統一管理使用者認證狀態，並在此處註冊 axios 攔截器以解決 HMR 問題
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  // 初始化：檢查是否已登入
  useEffect(() => {
    const initAuth = () => {
      const token = authService.getToken()
      const currentUser = authService.getCurrentUser()

      if (token && currentUser) {
        setUser(currentUser)
        setIsAuthenticated(true)
      }
      setIsLoading(false)
    }

    initAuth()
  }, [])

  // 註冊 axios 攔截器（在 React 元件內註冊，解決 HMR 問題）
  useEffect(() => {
    // 請求攔截器：自動添加 Authorization header
    const requestInterceptor = apiClient.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('adminToken')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // 回應攔截器：處理認證失效（雖然後端不會回傳 401，但保留以防萬一）
    const responseInterceptor = apiClient.interceptors.response.use(
      (response) => response,
      (error) => {
        // 如果收到認證相關錯誤，自動登出
        if (error.response?.status === 401 || error.response?.status === 403) {
          // 清除認證狀態
          logout()
        }
        return Promise.reject(error)
      }
    )

    // Cleanup：移除攔截器（HMR 時會重新註冊）
    return () => {
      apiClient.interceptors.request.eject(requestInterceptor)
      apiClient.interceptors.response.eject(responseInterceptor)
    }
  }, []) // 空依賴，只在掛載時執行

  /**
   * 登入
   */
  const login = async (credentials) => {
    const data = await authService.login(credentials)

    // 更新狀態
    setUser(data.profile || { username: credentials.username })
    setIsAuthenticated(true)

    return data
  }

  /**
   * 登出
   */
  const logout = () => {
    // 清除 localStorage
    authService.logout()

    // 清除狀態
    setUser(null)
    setIsAuthenticated(false)

    // 導向登入頁
    window.location.href = '/'
  }

  const value = {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

/**
 * useAuth Hook
 * 在元件中使用此 Hook 取得認證狀態和方法
 */
export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth 必須在 AuthProvider 內部使用')
  }

  return context
}

export default AuthContext
