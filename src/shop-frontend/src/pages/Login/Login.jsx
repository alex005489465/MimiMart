/**
 * 會員登入頁面
 */
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { validation } from '../../utils/validation';
import styles from './Login.module.css';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

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

    const emailError = validation.getEmailError(formData.email);
    if (emailError) newErrors.email = emailError;

    const passwordError = validation.getPasswordError(formData.password);
    if (passwordError) newErrors.password = passwordError;

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

    try {
      const result = await login({
        email: formData.email,
        password: formData.password,
      });

      if (result.success) {
        // 登入成功,導向首頁
        navigate('/');
      } else {
        setErrorMessage(result.message || '登入失敗,請檢查帳號密碼');
      }
    } catch (error) {
      setErrorMessage('登入失敗,請稍後再試');
      console.error('登入錯誤:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.loginPage}>
      <div className={styles.loginContainer}>
        <div className={styles.loginCard}>
          <h1 className={styles.title}>會員登入</h1>
          <p className={styles.subtitle}>歡迎回到 MimiMart</p>

          {errorMessage && (
            <div className={styles.errorAlert}>{errorMessage}</div>
          )}

          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.formGroup}>
              <label htmlFor="email" className={styles.label}>
                Email
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
                密碼
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
                placeholder="請輸入您的密碼"
                disabled={isSubmitting}
              />
              {errors.password && (
                <span className={styles.errorText}>{errors.password}</span>
              )}
            </div>

            <div className={styles.formOptions}>
              <label className={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  name="rememberMe"
                  checked={formData.rememberMe}
                  onChange={handleChange}
                  className={styles.checkbox}
                  disabled={isSubmitting}
                />
                <span>記住我</span>
              </label>
              <Link to="/forgot-password" className={styles.forgotLink}>
                忘記密碼?
              </Link>
            </div>

            <button
              type="submit"
              className={styles.submitButton}
              disabled={isSubmitting}
            >
              {isSubmitting ? '登入中...' : '登入'}
            </button>
          </form>

          <div className={styles.footer}>
            <p>
              還沒有帳號?{' '}
              <Link to="/register" className={styles.registerLink}>
                立即註冊
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
