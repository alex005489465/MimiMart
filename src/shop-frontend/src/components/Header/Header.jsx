import { Link, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import ThemeToggle from '../ThemeToggle/ThemeToggle'
import styles from './Header.module.css'

export default function Header() {
  const navigate = useNavigate()
  const { user, isAuthenticated, logout } = useAuth()
  const [showDropdown, setShowDropdown] = useState(false)

  const handleLogout = () => {
    logout()
    setShowDropdown(false)
    navigate('/')
  }

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
          <ThemeToggle />
          <Link to="/cart" className={styles.cartLink}>
            <span className={styles.cartIcon}>🛒</span>
            <span className={styles.cartBadge}>0</span>
          </Link>

          {/* 會員狀態判斷 */}
          {isAuthenticated ? (
            <div
              className={styles.userMenu}
              onMouseEnter={() => setShowDropdown(true)}
              onMouseLeave={() => setShowDropdown(false)}
            >
              <button className={styles.userButton}>
                <span className={styles.userAvatar}>
                  {user?.name?.charAt(0)?.toUpperCase() || 'M'}
                </span>
                <span className={styles.userName}>{user?.name}</span>
                <span className={styles.dropdownIcon}>▼</span>
              </button>

              {showDropdown && (
                <div className={styles.dropdown}>
                  <Link
                    to="/member"
                    className={styles.dropdownItem}
                    onClick={() => setShowDropdown(false)}
                  >
                    <span>👤</span>
                    <span>會員資料</span>
                  </Link>
                  <button
                    className={styles.dropdownItem}
                    onClick={handleLogout}
                  >
                    <span>🚪</span>
                    <span>登出</span>
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link to="/login" className={styles.loginLink}>
              登入 / 註冊
            </Link>
          )}
        </div>
      </div>
    </header>
  )
}
