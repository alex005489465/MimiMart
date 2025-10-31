import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import ThemeToggle from '../../components/ThemeToggle/ThemeToggle'
import styles from './Dashboard.module.css'

/**
 * 管理後台儀表板頁面
 */
function Dashboard() {
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
    // 清除登入狀態
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('adminUser')

    // 導向登入頁
    navigate('/')
  }

  if (!adminUser) {
    return null // 或顯示載入中畫面
  }

  return (
    <div className={styles.dashboardContainer}>
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
            <a href="#" className={`${styles.navLink} ${styles.active}`}>
              <span className={styles.navIcon}>📊</span>
              儀表板
            </a>
          </div>

          <div className={styles.navSection}>
            <h3 className={styles.navTitle}>商品管理</h3>
            <a href="#" className={styles.navLink}>
              <span className={styles.navIcon}>📦</span>
              商品列表
            </a>
            <a href="#" className={styles.navLink}>
              <span className={styles.navIcon}>📁</span>
              分類管理
            </a>
          </div>

          <div className={styles.navSection}>
            <h3 className={styles.navTitle}>訂單管理</h3>
            <a href="#" className={styles.navLink}>
              <span className={styles.navIcon}>🛒</span>
              訂單列表
            </a>
          </div>

          <div className={styles.navSection}>
            <h3 className={styles.navTitle}>系統設定</h3>
            <a href="#" className={styles.navLink}>
              <span className={styles.navIcon}>👥</span>
              管理員管理
            </a>
            <a href="#" className={styles.navLink}>
              <span className={styles.navIcon}>⚙️</span>
              系統設定
            </a>
          </div>
        </nav>
      </aside>

      {/* 主要內容區 */}
      <main className={styles.mainContent}>
        <div className={styles.contentHeader}>
          <h2 className={styles.pageTitle}>儀表板</h2>
          <p className={styles.pageDescription}>歡迎回來，{adminUser.username}！</p>
        </div>

        {/* 統計卡片 */}
        <div className={styles.statsGrid}>
          <div className={styles.statCard}>
            <div className={styles.statIcon} style={{background: '#dbeafe'}}>
              📦
            </div>
            <div className={styles.statContent}>
              <p className={styles.statLabel}>商品總數</p>
              <p className={styles.statValue}>--</p>
            </div>
          </div>

          <div className={styles.statCard}>
            <div className={styles.statIcon} style={{background: '#fef3c7'}}>
              🛒
            </div>
            <div className={styles.statContent}>
              <p className={styles.statLabel}>待處理訂單</p>
              <p className={styles.statValue}>--</p>
            </div>
          </div>

          <div className={styles.statCard}>
            <div className={styles.statIcon} style={{background: '#d1fae5'}}>
              👥
            </div>
            <div className={styles.statContent}>
              <p className={styles.statLabel}>會員總數</p>
              <p className={styles.statValue}>--</p>
            </div>
          </div>

          <div className={styles.statCard}>
            <div className={styles.statIcon} style={{background: '#fce7f3'}}>
              💰
            </div>
            <div className={styles.statContent}>
              <p className={styles.statLabel}>本月營收</p>
              <p className={styles.statValue}>--</p>
            </div>
          </div>
        </div>

        {/* 快速操作區 */}
        <div className={styles.quickActions}>
          <h3 className={styles.sectionTitle}>快速操作</h3>
          <div className={styles.actionGrid}>
            <button className={styles.actionButton}>
              <span className={styles.actionIcon}>➕</span>
              <span className={styles.actionText}>新增商品</span>
            </button>
            <button className={styles.actionButton}>
              <span className={styles.actionIcon}>📝</span>
              <span className={styles.actionText}>查看訂單</span>
            </button>
            <button className={styles.actionButton}>
              <span className={styles.actionIcon}>👤</span>
              <span className={styles.actionText}>會員管理</span>
            </button>
            <button className={styles.actionButton}>
              <span className={styles.actionIcon}>📊</span>
              <span className={styles.actionText}>銷售報表</span>
            </button>
          </div>
        </div>

        {/* 提示訊息 */}
        <div className={styles.infoBox}>
          <h3 className={styles.infoTitle}>開發提示</h3>
          <p className={styles.infoText}>
            這是管理後台的儀表板頁面。目前顯示的是靜態內容，後續需要整合實際的 API 來顯示即時數據。
          </p>
          <p className={styles.infoText}>
            建議的後續開發步驟：
          </p>
          <ul className={styles.infoList}>
            <li>整合後端管理 API（/api/admin/*）</li>
            <li>實作商品管理功能（CRUD）</li>
            <li>實作訂單管理功能</li>
            <li>實作管理員權限控制</li>
            <li>新增儀表板統計圖表</li>
          </ul>
        </div>
      </main>
    </div>
  )
}

export default Dashboard
