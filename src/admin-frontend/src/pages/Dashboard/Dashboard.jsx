import { useAuth } from '@/context/AuthContext'
import styles from './Dashboard.module.css'

/**
 * 管理後台儀表板頁面（內容區）
 */
function Dashboard() {
  const { user } = useAuth()

  return (
    <div className={styles.dashboardContent}>
      <div className={styles.contentHeader}>
        <h2 className={styles.pageTitle}>儀表板</h2>
        <p className={styles.pageDescription}>
          歡迎回來{user ? `，${user.username}` : ''}！
        </p>
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
    </div>
  )
}

export default Dashboard
