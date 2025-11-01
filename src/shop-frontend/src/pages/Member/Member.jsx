/**
 * 會員資料主頁面 (分頁管理型)
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { memberService } from '../../services/memberService';
import ProfileView from './components/ProfileView';
import ProfileEdit from './components/ProfileEdit';
import PasswordChange from './components/PasswordChange';
import styles from './Member.module.css';

const Member = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [activeTab, setActiveTab] = useState('profile');

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'profile':
        return <ProfileView user={user} />;
      case 'edit':
        return <ProfileEdit user={user} onSuccess={() => setActiveTab('profile')} />;
      case 'password':
        return <PasswordChange />;
      default:
        return <ProfileView user={user} />;
    }
  };

  return (
    <div className={styles.memberPage}>
      <div className={styles.container}>
        {/* 左側選單 */}
        <aside className={styles.sidebar}>
          <div className={styles.userInfo}>
            <div className={styles.avatar}>
              {user?.avatarUrl ? (
                <img
                  src={memberService.getAvatarUrl(user.id)}
                  alt="會員頭像"
                  className={styles.avatarImage}
                  onError={(e) => {
                    e.target.style.display = 'none';
                    e.target.parentElement.innerText =
                      user?.name?.charAt(0)?.toUpperCase() || 'M';
                  }}
                />
              ) : (
                user?.name?.charAt(0)?.toUpperCase() || 'M'
              )}
            </div>
            <h2 className={styles.userName}>{user?.name}</h2>
            <p className={styles.userEmail}>{user?.email}</p>
          </div>

          <nav className={styles.nav}>
            <button
              className={`${styles.navItem} ${
                activeTab === 'profile' ? styles.navItemActive : ''
              }`}
              onClick={() => setActiveTab('profile')}
            >
              <span className={styles.navIcon}>👤</span>
              <span>個人資料</span>
            </button>

            <button
              className={`${styles.navItem} ${
                activeTab === 'edit' ? styles.navItemActive : ''
              }`}
              onClick={() => setActiveTab('edit')}
            >
              <span className={styles.navIcon}>✏️</span>
              <span>編輯資料</span>
            </button>

            <button
              className={`${styles.navItem} ${
                activeTab === 'password' ? styles.navItemActive : ''
              }`}
              onClick={() => setActiveTab('password')}
            >
              <span className={styles.navIcon}>🔒</span>
              <span>修改密碼</span>
            </button>

            <button
              className={`${styles.navItem} ${styles.navItemLogout}`}
              onClick={handleLogout}
            >
              <span className={styles.navIcon}>🚪</span>
              <span>登出</span>
            </button>
          </nav>
        </aside>

        {/* 右側內容區 */}
        <main className={styles.content}>{renderContent()}</main>
      </div>
    </div>
  );
};

export default Member;
