/**
 * 個人資料檢視元件
 */
import { useState, useEffect } from 'react';
import { memberService } from '../../../services/memberService';
import styles from './ProfileView.module.css';

const ProfileView = ({ user }) => {
  const [avatarBlobUrl, setAvatarBlobUrl] = useState(null);

  // 載入頭像
  useEffect(() => {
    const loadAvatar = async () => {
      if (user?.id && user?.avatarUrl) {
        try {
          const blobUrl = await memberService.getAvatarUrl(user.id);
          setAvatarBlobUrl(blobUrl);
        } catch (error) {
          console.error('載入頭像失敗:', error);
        }
      }
    };

    loadAvatar();

    // 清理 Blob URL
    return () => {
      if (avatarBlobUrl) {
        URL.revokeObjectURL(avatarBlobUrl);
      }
    };
  }, [user?.id, user?.avatarUrl]);

  return (
    <div className={styles.profileView}>
      <h1 className={styles.title}>個人資料</h1>
      <p className={styles.subtitle}>查看您的帳號資訊</p>

      {/* 頭像顯示區 */}
      <div className={styles.avatarSection}>
        <div className={styles.avatarDisplay}>
          {avatarBlobUrl ? (
            <img
              src={avatarBlobUrl}
              alt="會員頭像"
              className={styles.avatarImage}
            />
          ) : (
            <div className={styles.avatarPlaceholder}>
              {user?.name?.charAt(0)?.toUpperCase() || 'M'}
            </div>
          )}
        </div>
        {user?.avatarUpdatedAt && (
          <p className={styles.avatarUpdateTime}>
            頭像更新於: {new Date(user.avatarUpdatedAt).toLocaleString('zh-TW')}
          </p>
        )}
      </div>

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
