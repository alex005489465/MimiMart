/**
 * 會員註冊頁面
 */
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { validation } from '../../utils/validation';
import styles from './Register.module.css';

const Register = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreeToTerms: false,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // 處理輸入變更
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));

    // 清除該欄位的錯誤訊息
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
    setErrorMessage('');
  };

  // 驗證表單
  const validateForm = () => {
    const newErrors = {};

    const nameError = validation.getNameError(formData.name);
    if (nameError) newErrors.name = nameError;

    const emailError = validation.getEmailError(formData.email);
    if (emailError) newErrors.email = emailError;

    const passwordError = validation.getPasswordError(formData.password);
    if (passwordError) newErrors.password = passwordError;

    const confirmPasswordError = validation.getConfirmPasswordError(
      formData.password,
      formData.confirmPassword
    );
    if (confirmPasswordError)
      newErrors.confirmPassword = confirmPasswordError;

    if (!formData.agreeToTerms) {
      newErrors.agreeToTerms = '請閱讀並同意服務條款';
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
      const result = await register({
        name: formData.name,
        email: formData.email,
        password: formData.password,
      });

      if (result.success) {
        setSuccessMessage('註冊成功!即將跳轉到登入頁面...');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setErrorMessage(result.message || '註冊失敗,請稍後再試');
      }
    } catch (error) {
      setErrorMessage('註冊失敗,請稍後再試');
      console.error('註冊錯誤:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.registerPage}>
      <div className={styles.registerContainer}>
        <div className={styles.registerCard}>
          <h1 className={styles.title}>會員註冊</h1>
          <p className={styles.subtitle}>加入 MimiMart 開始購物</p>

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
                className={`${styles.input} ${
                  errors.name ? styles.inputError : ''
                }`}
                placeholder="請輸入您的姓名"
                disabled={isSubmitting}
              />
              {errors.name && (
                <span className={styles.errorText}>{errors.name}</span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="email" className={styles.label}>
                Email *
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className={`${styles.input} ${
                  errors.email ? styles.inputError : ''
                }`}
                placeholder="請輸入您的 Email"
                disabled={isSubmitting}
              />
              {errors.email && (
                <span className={styles.errorText}>{errors.email}</span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="password" className={styles.label}>
                密碼 *
              </label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className={`${styles.input} ${
                  errors.password ? styles.inputError : ''
                }`}
                placeholder="請輸入密碼 (6-50 個字元)"
                disabled={isSubmitting}
              />
              {errors.password && (
                <span className={styles.errorText}>{errors.password}</span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="confirmPassword" className={styles.label}>
                確認密碼 *
              </label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className={`${styles.input} ${
                  errors.confirmPassword ? styles.inputError : ''
                }`}
                placeholder="請再次輸入密碼"
                disabled={isSubmitting}
              />
              {errors.confirmPassword && (
                <span className={styles.errorText}>
                  {errors.confirmPassword}
                </span>
              )}
            </div>

            <div className={styles.termsGroup}>
              <label className={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  name="agreeToTerms"
                  checked={formData.agreeToTerms}
                  onChange={handleChange}
                  className={styles.checkbox}
                  disabled={isSubmitting}
                />
                <span>
                  我已閱讀並同意{' '}
                  <Link to="/terms" className={styles.link}>
                    服務條款
                  </Link>{' '}
                  和{' '}
                  <Link to="/privacy" className={styles.link}>
                    隱私權政策
                  </Link>
                </span>
              </label>
              {errors.agreeToTerms && (
                <span className={styles.errorText}>{errors.agreeToTerms}</span>
              )}
            </div>

            <button
              type="submit"
              className={styles.submitButton}
              disabled={isSubmitting}
            >
              {isSubmitting ? '註冊中...' : '註冊'}
            </button>
          </form>

          <div className={styles.footer}>
            <p>
              已經有帳號?{' '}
              <Link to="/login" className={styles.loginLink}>
                立即登入
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
