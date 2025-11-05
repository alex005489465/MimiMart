import { useEffect, useState } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import authService from '@/services/authService'
import ThemeToggle from '../ThemeToggle/ThemeToggle'
import styles from './Layout.module.css'

/**
 * 管理後台共用佈局元件
 * 包含頂部導航列、側邊欄和主要內容區
 */
function Layout() {
  const navigate = useNavigate()
  const [adminUser, setAdminUser] = useState(null)

  useEffect(() => {
    // 檢查登入狀態
    const isAuthenticated = localStorage.getItem('isAuthenticated')
    const userStr = localStorage.getItem('adminUser')

    if (!isAuthenticated || !userStr) {
      // 未登入，導向登入頁
      navigate('/')
      return
    }

    try {
      const user = JSON.parse(userStr)
      setAdminUser(user)
    } catch (err) {
      console.error('解析使用者資料失敗:', err)
      navigate('/')
    }
  }, [navigate])

  /**
   * 處理登出
   */
  const handleLogout = () => {
    authService.logout()
    navigate('/')
  }

  if (!adminUser) {
    return null // 或顯示載入中畫面
  }

  return (
    <div className={styles.layoutContainer}>
      {/* 頂部導航列 */}
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1 className={styles.logo}>MimiMart 管理後台</h1>
        </div>
        <div className={styles.headerRight}>
          <ThemeToggle />
          <div className={styles.userInfo}>
            <span className={styles.username}>{adminUser.username}</span>
          </div>
          <button
            className={styles.logoutButton}
            onClick={handleLogout}
          >
            登出
          </button>
        </div>
      </header>

      {/* 側邊欄 */}
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <div className={styles.navSection}>
            <h3 className={styles.navTitle}>主選單</h3>
            <NavLink
              to="/dashboard"
              className={({ isActive }) =>
                `${styles.navLink} ${isActive ? styles.active : ''}`
              }
            >
              <span className={styles.navIcon}>📊</span>
              儀表板
            </NavLink>
          </div>

          <div className={styles.navSection}>
            <h3 className={styles.navTitle}>商品管理</h3>
            <NavLink to="#" className={styles.navLink}>
              <span className={styles.navIcon}>📦</span>
              商品列表
            </NavLink>
            <NavLink to="#" className={styles.navLink}>
              <span className={styles.navIcon}>📁</span>
              分類管理
            </NavLink>
          </div>

          <div className={styles.navSection}>
            <h3 className={styles.navTitle}>訂單管理</h3>
            <NavLink to="#" className={styles.navLink}>
              <span className={styles.navIcon}>🛒</span>
              訂單列表
            </NavLink>
          </div>

          <div className={styles.navSection}>
            <h3 className={styles.navTitle}>系統設定</h3>
            <NavLink
              to="/banners"
              className={({ isActive }) =>
                `${styles.navLink} ${isActive ? styles.active : ''}`
              }
            >
              <span className={styles.navIcon}>🖼️</span>
              Banner 管理
            </NavLink>
            <NavLink to="#" className={styles.navLink}>
              <span className={styles.navIcon}>👥</span>
              管理員管理
            </NavLink>
            <NavLink to="#" className={styles.navLink}>
              <span className={styles.navIcon}>⚙️</span>
              系統設定
            </NavLink>
          </div>
        </nav>
      </aside>

      {/* 主要內容區（子路由渲染區） */}
      <main className={styles.mainContent}>
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
