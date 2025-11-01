/**
 * 編輯個人資料元件
 */
import { useState } from 'react';
import { useAuth } from '../../../hooks/useAuth';
import { memberService } from '../../../services/memberService';
import { validation } from '../../../utils/validation';
import AvatarUpload from './AvatarUpload';
import styles from './ProfileEdit.module.css';

const ProfileEdit = ({ user, onSuccess }) => {
  const { updateUserProfile } = useAuth();

  const [formData, setFormData] = useState({
    name: user?.name || '',
    phone: user?.phone || '',
    homeAddress: user?.homeAddress || '',
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // 處理輸入變更
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // 清除該欄位的錯誤訊息
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
    setErrorMessage('');
    setSuccessMessage('');
  };

  // 驗證表單
  const validateForm = () => {
    const newErrors = {};

    const nameError = validation.getNameError(formData.name);
    if (nameError) newErrors.name = nameError;

    const phoneError = validation.getPhoneError(formData.phone);
    if (phoneError) newErrors.phone = phoneError;

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 處理表單提交
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const response = await memberService.updateProfile(formData);

      if (response.success) {
        setSuccessMessage('個人資料更新成功!');

        // 更新 Context 中的使用者資料
        const updatedUser = { ...user, ...formData };
        updateUserProfile(updatedUser);

        // 2 秒後切換回檢視模式
        setTimeout(() => {
          onSuccess();
        }, 2000);
      } else {
        setErrorMessage(response.message || '更新失敗,請稍後再試');
      }
    } catch (error) {
      setErrorMessage(error.message || '更新失敗,請稍後再試');
      console.error('更新個人資料錯誤:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.profileEdit}>
      <h1 className={styles.title}>編輯個人資料</h1>
      <p className={styles.subtitle}>更新您的帳號資訊</p>

      {/* 頭像上傳區 */}
      <div className={styles.avatarSection}>
        <AvatarUpload />
      </div>

      {errorMessage && (
        <div className={styles.errorAlert}>{errorMessage}</div>
      )}

      {successMessage && (
        <div className={styles.successAlert}>{successMessage}</div>
      )}

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.formGroup}>
          <label htmlFor="name" className={styles.label}>
            姓名 *
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            className={`${styles.input} ${errors.name ? styles.inputError : ''}`}
            placeholder="請輸入姓名"
            disabled={isSubmitting}
          />
          {errors.name && (
            <span className={styles.errorText}>{errors.name}</span>
          )}
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="phone" className={styles.label}>
            電話
          </label>
          <input
            type="tel"
            id="phone"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            className={`${styles.input} ${errors.phone ? styles.inputError : ''}`}
            placeholder="請輸入電話 (09 開頭的 10 碼數字)"
            disabled={isSubmitting}
          />
          {errors.phone && (
            <span className={styles.errorText}>{errors.phone}</span>
          )}
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="homeAddress" className={styles.label}>
            地址
          </label>
          <textarea
            id="homeAddress"
            name="homeAddress"
            value={formData.homeAddress}
            onChange={handleChange}
            className={styles.textarea}
            placeholder="請輸入地址"
            rows="3"
            disabled={isSubmitting}
          />
        </div>

        <div className={styles.formActions}>
          <button
            type="button"
            className={styles.cancelButton}
            onClick={onSuccess}
            disabled={isSubmitting}
          >
            取消
          </button>
          <button
            type="submit"
            className={styles.submitButton}
            disabled={isSubmitting}
          >
            {isSubmitting ? '更新中...' : '儲存'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ProfileEdit;
