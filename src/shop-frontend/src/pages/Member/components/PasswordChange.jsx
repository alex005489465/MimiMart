/**
 * 修改密碼元件
 */
import { useState } from 'react';
import { memberService } from '../../../services/memberService';
import { validation } from '../../../utils/validation';
import styles from './PasswordChange.module.css';

const PasswordChange = () => {
  const [formData, setFormData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmNewPassword: '',
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

    if (!validation.isRequired(formData.oldPassword)) {
      newErrors.oldPassword = '請輸入舊密碼';
    }

    const newPasswordError = validation.getPasswordError(formData.newPassword);
    if (newPasswordError) newErrors.newPassword = newPasswordError;

    const confirmPasswordError = validation.getConfirmPasswordError(
      formData.newPassword,
      formData.confirmNewPassword
    );
    if (confirmPasswordError)
      newErrors.confirmNewPassword = confirmPasswordError;

    if (
      formData.oldPassword &&
      formData.newPassword &&
      formData.oldPassword === formData.newPassword
    ) {
      newErrors.newPassword = '新密碼不能與舊密碼相同';
    }

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
      const response = await memberService.changePassword({
        oldPassword: formData.oldPassword,
        newPassword: formData.newPassword,
      });

      if (response.success) {
        setSuccessMessage('密碼修改成功!');

        // 清空表單
        setFormData({
          oldPassword: '',
          newPassword: '',
          confirmNewPassword: '',
        });
      } else {
        setErrorMessage(response.message || '密碼修改失敗,請檢查舊密碼是否正確');
      }
    } catch (error) {
      setErrorMessage(error.message || '密碼修改失敗,請稍後再試');
      console.error('修改密碼錯誤:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.passwordChange}>
      <h1 className={styles.title}>修改密碼</h1>
      <p className={styles.subtitle}>為了您的帳號安全,請定期更換密碼</p>

      {errorMessage && (
        <div className={styles.errorAlert}>{errorMessage}</div>
      )}

      {successMessage && (
        <div className={styles.successAlert}>{successMessage}</div>
      )}

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.formGroup}>
          <label htmlFor="oldPassword" className={styles.label}>
            舊密碼 *
          </label>
          <input
            type="password"
            id="oldPassword"
            name="oldPassword"
            value={formData.oldPassword}
            onChange={handleChange}
            className={`${styles.input} ${
              errors.oldPassword ? styles.inputError : ''
            }`}
            placeholder="請輸入您的舊密碼"
            disabled={isSubmitting}
          />
          {errors.oldPassword && (
            <span className={styles.errorText}>{errors.oldPassword}</span>
          )}
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="newPassword" className={styles.label}>
            新密碼 *
          </label>
          <input
            type="password"
            id="newPassword"
            name="newPassword"
            value={formData.newPassword}
            onChange={handleChange}
            className={`${styles.input} ${
              errors.newPassword ? styles.inputError : ''
            }`}
            placeholder="請輸入新密碼 (6-50 個字元)"
            disabled={isSubmitting}
          />
          {errors.newPassword && (
            <span className={styles.errorText}>{errors.newPassword}</span>
          )}
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="confirmNewPassword" className={styles.label}>
            確認新密碼 *
          </label>
          <input
            type="password"
            id="confirmNewPassword"
            name="confirmNewPassword"
            value={formData.confirmNewPassword}
            onChange={handleChange}
            className={`${styles.input} ${
              errors.confirmNewPassword ? styles.inputError : ''
            }`}
            placeholder="請再次輸入新密碼"
            disabled={isSubmitting}
          />
          {errors.confirmNewPassword && (
            <span className={styles.errorText}>
              {errors.confirmNewPassword}
            </span>
          )}
        </div>

        <button
          type="submit"
          className={styles.submitButton}
          disabled={isSubmitting}
        >
          {isSubmitting ? '修改中...' : '確認修改'}
        </button>
      </form>

      <div className={styles.tips}>
        <h3 className={styles.tipsTitle}>密碼安全提示</h3>
        <ul className={styles.tipsList}>
          <li>密碼長度至少需要 6 個字元</li>
          <li>建議使用大小寫字母、數字和特殊符號的組合</li>
          <li>不要使用生日、電話號碼等容易猜測的密碼</li>
          <li>定期更換密碼,保護帳號安全</li>
        </ul>
      </div>
    </div>
  );
};

export default PasswordChange;
