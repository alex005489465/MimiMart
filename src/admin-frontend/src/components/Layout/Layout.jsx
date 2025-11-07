import { useState, useEffect } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import ThemeToggle from '../ThemeToggle/ThemeToggle'
import styles from './Layout.module.css'
import desktopStyles from './Layout.desktop.module.css'
import tabletStyles from './Layout.tablet.module.css'
import mobileStyles from './Layout.mobile.module.css'

/**
 * 管理後台共用佈局元件
 * 包含頂部導航列、側邊欄和主要內容區
 */
function Layout() {
  const navigate = useNavigate()
  const { user, isAuthenticated, isLoading, logout } = useAuth()
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false)

  // 合併 CSS 類別的輔助函數
  const cx = (...classNames) => classNames.filter(Boolean).join(' ')
  const mergeStyles = (className) => cx(
    styles[className],
    desktopStyles[className],
    tabletStyles[className],
    mobileStyles[className]
  )

  // 檢查登入狀態
  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/')
    }
  }, [isAuthenticated, isLoading, navigate])

  /**
   * 切換側邊欄開關
   */
  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen)
  }

  /**
   * 關閉側邊欄
   */
  const closeSidebar = () => {
    setIsSidebarOpen(false)
  }

  /**
   * 切換使用者選單
   */
  const toggleUserMenu = () => {
    setIsUserMenuOpen(!isUserMenuOpen)
  }

  /**
   * 處理登出
   */
  const handleLogout = () => {
    logout()
  }

  if (isLoading || !user) {
    return null // 或顯示載入中畫面
  }

  return (
    <div className={`${styles.layoutContainer} ${desktopStyles.layoutContainer} ${tabletStyles.layoutContainer} ${mobileStyles.layoutContainer}`}>
      {/* 頂部導航列 */}
      <header className={`${styles.header} ${desktopStyles.header} ${tabletStyles.header} ${mobileStyles.header}`}>
        <div className={styles.headerLeft}>
          {/* 漢堡選單按鈕（手機版） */}
          <button
            className={`${styles.hamburgerButton} ${desktopStyles.hamburgerButton} ${tabletStyles.hamburgerButton} ${mobileStyles.hamburgerButton}`}
            onClick={toggleSidebar}
            aria-label="開關選單"
          >
            <span className={styles.hamburgerLine}></span>
            <span className={styles.hamburgerLine}></span>
            <span className={styles.hamburgerLine}></span>
          </button>
          <h1 className={`${styles.logo} ${desktopStyles.logo} ${tabletStyles.logo} ${mobileStyles.logo}`}>MimiMart 管理後台</h1>
        </div>
        <div className={`${styles.headerRight} ${desktopStyles.headerRight} ${tabletStyles.headerRight} ${mobileStyles.headerRight}`}>
          {/* 桌面版：顯示完整的主題切換和登出按鈕 */}
          <div className={`${styles.desktopOnly} ${desktopStyles.desktopOnly} ${tabletStyles.desktopOnly} ${mobileStyles.desktopOnly}`}>
            <ThemeToggle />
          </div>

          {/* 使用者選單 */}
          <div className={styles.userMenuContainer}>
            <button
              className={styles.userMenuButton}
              onClick={toggleUserMenu}
            >
              <span className={styles.username}>{user.username}</span>
              <span className={styles.dropdownIcon}>▼</span>
            </button>

            {/* 下拉選單 */}
            {isUserMenuOpen && (
              <div className={styles.userMenuDropdown}>
                <div className={styles.userMenuHeader}>
                  <span className={styles.userMenuName}>{user.username}</span>
                </div>
                <div className={styles.userMenuDivider}></div>
                <div className={`${styles.mobileOnly} ${desktopStyles.mobileOnly} ${tabletStyles.mobileOnly} ${mobileStyles.mobileOnly}`}>
                  <div className={styles.userMenuItem}>
                    <span className={styles.userMenuLabel}>主題</span>
                    <ThemeToggle />
                  </div>
                  <div className={styles.userMenuDivider}></div>
                </div>
                <button
                  className={styles.userMenuItem}
                  onClick={handleLogout}
                >
                  <span className={styles.logoutIcon}>🚪</span>
                  登出
                </button>
              </div>
            )}
          </div>

          {/* 桌面版登出按鈕 */}
          <button
            className={`${styles.logoutButton} ${desktopStyles.logoutButton} ${tabletStyles.logoutButton} ${mobileStyles.logoutButton} ${styles.desktopOnly} ${desktopStyles.desktopOnly} ${tabletStyles.desktopOnly} ${mobileStyles.desktopOnly}`}
            onClick={handleLogout}
          >
            登出
          </button>
        </div>
      </header>

      {/* 遮罩層（手機版） */}
      {isSidebarOpen && (
        <div
          className={`${styles.overlay} ${desktopStyles.overlay} ${tabletStyles.overlay} ${mobileStyles.overlay}`}
          onClick={closeSidebar}
        ></div>
      )}

      {/* 側邊欄 */}
      <aside className={`${styles.sidebar} ${desktopStyles.sidebar} ${tabletStyles.sidebar} ${mobileStyles.sidebar} ${isSidebarOpen ? mobileStyles.sidebarOpen : ''}`}>
        <nav className={`${styles.nav} ${desktopStyles.nav} ${tabletStyles.nav} ${mobileStyles.nav}`}>
          <div className={`${styles.navSection} ${desktopStyles.navSection} ${tabletStyles.navSection} ${mobileStyles.navSection}`}>
            <h3 className={`${styles.navTitle} ${desktopStyles.navTitle} ${tabletStyles.navTitle} ${mobileStyles.navTitle}`}>主選單</h3>
            <NavLink
              to="/dashboard"
              className={({ isActive }) =>
                `${styles.navLink} ${desktopStyles.navLink} ${tabletStyles.navLink} ${mobileStyles.navLink} ${isActive ? styles.active : ''}`
              }
              onClick={closeSidebar}
              title="儀表板"
            >
              <span className={`${styles.navIcon} ${desktopStyles.navIcon} ${tabletStyles.navIcon} ${mobileStyles.navIcon}`}>📊</span>
              <span className={`${styles.navText} ${desktopStyles.navText} ${tabletStyles.navText} ${mobileStyles.navText}`}>儀表板</span>
            </NavLink>
          </div>

          <div className={mergeStyles('navSection')}>
            <h3 className={mergeStyles('navTitle')}>商品管理</h3>
            <NavLink
              to="/products"
              className={({ isActive }) =>
                cx(mergeStyles('navLink'), isActive && styles.active)
              }
              onClick={closeSidebar}
              title="商品列表"
            >
              <span className={mergeStyles('navIcon')}>📦</span>
              <span className={mergeStyles('navText')}>商品列表</span>
            </NavLink>
            <NavLink
              to="/categories"
              className={({ isActive }) =>
                cx(mergeStyles('navLink'), isActive && styles.active)
              }
              onClick={closeSidebar}
              title="分類管理"
            >
              <span className={mergeStyles('navIcon')}>📁</span>
              <span className={mergeStyles('navText')}>分類管理</span>
            </NavLink>
          </div>

          <div className={mergeStyles('navSection')}>
            <h3 className={mergeStyles('navTitle')}>訂單管理</h3>
            <NavLink
              to="#"
              className={mergeStyles('navLink')}
              onClick={closeSidebar}
              title="訂單列表"
            >
              <span className={mergeStyles('navIcon')}>🛒</span>
              <span className={mergeStyles('navText')}>訂單列表</span>
            </NavLink>
          </div>

          <div className={mergeStyles('navSection')}>
            <h3 className={mergeStyles('navTitle')}>系統設定</h3>
            <NavLink
              to="/banners"
              className={({ isActive }) =>
                cx(mergeStyles('navLink'), isActive && styles.active)
              }
              onClick={closeSidebar}
              title="Banner 管理"
            >
              <span className={mergeStyles('navIcon')}>🖼️</span>
              <span className={mergeStyles('navText')}>Banner 管理</span>
            </NavLink>
            <NavLink
              to="#"
              className={mergeStyles('navLink')}
              onClick={closeSidebar}
              title="管理員管理"
            >
              <span className={mergeStyles('navIcon')}>👥</span>
              <span className={mergeStyles('navText')}>管理員管理</span>
            </NavLink>
            <NavLink
              to="#"
              className={mergeStyles('navLink')}
              onClick={closeSidebar}
              title="系統設定"
            >
              <span className={mergeStyles('navIcon')}>⚙️</span>
              <span className={mergeStyles('navText')}>系統設定</span>
            </NavLink>
          </div>
        </nav>
      </aside>

      {/* 主要內容區（子路由渲染區） */}
      <main className={mergeStyles('mainContent')}>
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
