import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import styles from './Login.module.css'

/**
 * 管理員登入頁面
 */
function Login() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  })
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  /**
   * 處理表單輸入變更
   */
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    // 清除錯誤訊息
    if (error) setError('')
  }

  /**
   * 處理表單提交
   */
  const handleSubmit = async (e) => {
    e.preventDefault()

    // 基本驗證
    if (!formData.username || !formData.password) {
      setError('請輸入帳號和密碼')
      return
    }

    setIsLoading(true)
    setError('')

    try {
      // TODO: 整合實際的登入 API
      // const response = await authService.login(formData)

      // 模擬 API 呼叫
      await new Promise(resolve => setTimeout(resolve, 1000))

      // 暫時儲存登入狀態（後續需要改用實際的 token 管理）
      localStorage.setItem('isAuthenticated', 'true')
      localStorage.setItem('adminUser', JSON.stringify({
        username: formData.username,
        loginTime: new Date().toISOString()
      }))

      // 導向儀表板
      navigate('/dashboard')
    } catch (err) {
      console.error('登入失敗:', err)
      setError(err.message || '登入失敗，請檢查帳號密碼')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className={styles.loginContainer}>
      <div className={styles.loginCard}>
        <div className={styles.loginHeader}>
          <h1 className={styles.title}>MimiMart</h1>
          <p className={styles.subtitle}>管理後台</p>
        </div>

        <form className={styles.loginForm} onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label htmlFor="username" className={styles.label}>
              帳號
            </label>
            <input
              type="text"
              id="username"
              name="username"
              className={styles.input}
              placeholder="請輸入管理員帳號"
              value={formData.username}
              onChange={handleChange}
              disabled={isLoading}
              autoComplete="username"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password" className={styles.label}>
              密碼
            </label>
            <input
              type="password"
              id="password"
              name="password"
              className={styles.input}
              placeholder="請輸入密碼"
              value={formData.password}
              onChange={handleChange}
              disabled={isLoading}
              autoComplete="current-password"
            />
          </div>

          {error && (
            <div className={styles.errorMessage}>
              {error}
            </div>
          )}

          <button
            type="submit"
            className={styles.submitButton}
            disabled={isLoading}
          >
            {isLoading ? '登入中...' : '登入'}
          </button>
        </form>

        <div className={styles.loginFooter}>
          <p className={styles.footerText}>
            MimiMart 管理系統 v1.0.0
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login
