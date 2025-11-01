/**
 * 頭像上傳元件
 */
import { useState, useRef } from 'react';
import { memberService } from '../../../services/memberService';
import { useAuth } from '../../../hooks/useAuth';
import styles from './AvatarUpload.module.css';

const AvatarUpload = () => {
  const { user, updateUserProfile } = useAuth();
  const fileInputRef = useRef(null);

  const [preview, setPreview] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [uploadKey, setUploadKey] = useState(0); // 用於強制重新載入圖片

  // 檔案驗證
  const validateFile = (file) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
    const maxSize = 5 * 1024 * 1024; // 5MB

    if (!allowedTypes.includes(file.type)) {
      return '僅支援 JPG、PNG、GIF 格式';
    }

    if (file.size > maxSize) {
      return '檔案大小不能超過 5MB';
    }

    return null;
  };

  // 處理檔案選擇
  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 驗證檔案
    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      setSuccess('');
      return;
    }

    // 本地預覽
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreview(reader.result);
    };
    reader.readAsDataURL(file);

    // 上傳檔案
    setIsUploading(true);
    setError('');
    setSuccess('');

    try {
      const response = await memberService.uploadAvatar(file);

      if (response.success) {
        setSuccess('頭像上傳成功!');

        // 更新使用者資料
        const updatedUser = {
          ...user,
          avatarUrl: response.data.avatarUrl,
          avatarUpdatedAt: response.data.avatarUpdatedAt,
        };
        updateUserProfile(updatedUser);

        // 清除預覽,強制重新載入圖片
        setPreview(null);
        setUploadKey((prev) => prev + 1);

        // 3 秒後清除成功訊息
        setTimeout(() => setSuccess(''), 3000);
      } else {
        setError(response.message || '上傳失敗,請稍後再試');
        setPreview(null);
      }
    } catch (err) {
      setError(err.message || '上傳失敗,請稍後再試');
      setPreview(null);
      console.error('頭像上傳錯誤:', err);
    } finally {
      setIsUploading(false);
      // 清空 input,讓同一檔案可以再次選擇
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  // 觸發檔案選擇
  const handleClick = () => {
    fileInputRef.current?.click();
  };

  // 取得當前頭像 URL
  const getCurrentAvatarUrl = () => {
    if (preview) return preview;
    if (user?.avatarUrl) {
      return `${memberService.getAvatarUrl(user.id)}&_=${uploadKey}`;
    }
    return null;
  };

  return (
    <div className={styles.avatarUpload}>
      <div className={styles.avatarPreview} onClick={handleClick}>
        {getCurrentAvatarUrl() ? (
          <img
            src={getCurrentAvatarUrl()}
            alt="頭像預覽"
            className={styles.avatarImage}
            onError={(e) => {
              e.target.style.display = 'none';
              const placeholder = document.createElement('div');
              placeholder.className = styles.avatarPlaceholder;
              placeholder.innerText =
                user?.name?.charAt(0)?.toUpperCase() || 'M';
              e.target.parentElement.prepend(placeholder);
            }}
          />
        ) : (
          <div className={styles.avatarPlaceholder}>
            {user?.name?.charAt(0)?.toUpperCase() || 'M'}
          </div>
        )}
        <div className={styles.avatarOverlay}>
          <span className={styles.uploadIcon}>📷</span>
          <span className={styles.uploadText}>
            {isUploading ? '上傳中...' : '更換頭像'}
          </span>
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/png,image/gif"
        onChange={handleFileChange}
        className={styles.fileInput}
        disabled={isUploading}
      />

      <p className={styles.hint}>支援 JPG、PNG、GIF 格式,最大 5MB</p>

      {error && <div className={styles.errorMessage}>{error}</div>}
      {success && <div className={styles.successMessage}>{success}</div>}
    </div>
  );
};

export default AvatarUpload;
