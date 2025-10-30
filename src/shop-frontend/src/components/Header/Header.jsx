import { Link } from 'react-router-dom'
import styles from './Header.module.css'

export default function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.container}>
        {/* Logo */}
        <Link to="/" className={styles.logo}>
          <span className={styles.logoIcon}>🛒</span>
          <span className={styles.logoText}>MimiMart</span>
        </Link>

        {/* 搜尋欄 */}
        <div className={styles.searchBar}>
          <input
            type="text"
            placeholder="搜尋商品..."
            className={styles.searchInput}
          />
          <button className={styles.searchButton}>
            <span>🔍</span>
          </button>
        </div>

        {/* 右側功能 */}
        <div className={styles.actions}>
          <Link to="/cart" className={styles.cartLink}>
            <span className={styles.cartIcon}>🛒</span>
            <span className={styles.cartBadge}>0</span>
          </Link>
          <Link to="/login" className={styles.loginLink}>
            登入 / 註冊
          </Link>
        </div>
      </div>
    </header>
  )
}
