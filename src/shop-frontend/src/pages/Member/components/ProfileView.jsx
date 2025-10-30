/**
 * 個人資料檢視元件
 */
import styles from './ProfileView.module.css';

const ProfileView = ({ user }) => {
  return (
    <div className={styles.profileView}>
      <h1 className={styles.title}>個人資料</h1>
      <p className={styles.subtitle}>查看您的帳號資訊</p>

      <div className={styles.infoSection}>
        <div className={styles.infoRow}>
          <label className={styles.label}>姓名</label>
          <div className={styles.value}>{user?.name || '未設定'}</div>
        </div>

        <div className={styles.infoRow}>
          <label className={styles.label}>Email</label>
          <div className={styles.value}>{user?.email}</div>
        </div>

        <div className={styles.infoRow}>
          <label className={styles.label}>電話</label>
          <div className={styles.value}>{user?.phone || '未設定'}</div>
        </div>

        <div className={styles.infoRow}>
          <label className={styles.label}>地址</label>
          <div className={styles.value}>
            {user?.homeAddress || '未設定'}
          </div>
        </div>

        <div className={styles.infoRow}>
          <label className={styles.label}>Email 驗證狀態</label>
          <div className={styles.value}>
            {user?.emailVerified ? (
              <span className={styles.verified}>✓ 已驗證</span>
            ) : (
              <span className={styles.unverified}>✗ 未驗證</span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfileView;
