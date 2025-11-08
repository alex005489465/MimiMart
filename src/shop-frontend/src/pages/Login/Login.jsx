/**
 * 會員登入/註冊頁面
 * 使用 MUI v6 元件，整合 Zustand authStore
 */
import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  TextField,
  Button,
  Checkbox,
  FormControlLabel,
  Tabs,
  Tab,
  Card,
  CardContent,
  Typography,
  Alert,
  Snackbar,
  Box,
  Link,
  InputAdornment,
  IconButton,
} from '@mui/material';
import { MdPerson, MdLock, MdEmail, MdVisibility, MdVisibilityOff } from 'react-icons/md';
import useAuthStore from '../../stores/authStore';
import styles from './Login.module.css';

// TabPanel 元件
function TabPanel({ children, value, index, ...other }) {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`tabpanel-${index}`}
      aria-labelledby={`tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
}

const Login = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login, register, isAuthenticated } = useAuthStore();

  // Tab 狀態
  const [activeTab, setActiveTab] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Snackbar 狀態
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  // 登入表單狀態
  const [loginData, setLoginData] = useState({
    email: '',
    password: '',
    remember: false,
  });
  const [loginErrors, setLoginErrors] = useState({});
  const [showLoginPassword, setShowLoginPassword] = useState(false);

  // 註冊表單狀態
  const [registerData, setRegisterData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreement: false,
  });
  const [registerErrors, setRegisterErrors] = useState({});
  const [showRegisterPassword, setShowRegisterPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // 從 URL 參數讀取 tab（例如：/login?tab=register）
  useEffect(() => {
    const tab = searchParams.get('tab');
    if (tab === 'register') {
      setActiveTab(1);
    }
  }, [searchParams]);

  // 如果已登入，導向首頁
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  // 登入表單驗證
  const validateLoginForm = () => {
    const errors = {};

    if (!loginData.email) {
      errors.email = '請輸入 Email';
    } else if (!/\S+@\S+\.\S+/.test(loginData.email)) {
      errors.email = '請輸入有效的 Email';
    }

    if (!loginData.password) {
      errors.password = '請輸入密碼';
    } else if (loginData.password.length < 6) {
      errors.password = '密碼至少需要 6 個字元';
    }

    setLoginErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // 註冊表單驗證
  const validateRegisterForm = () => {
    const errors = {};

    if (!registerData.username) {
      errors.username = '請輸入使用者名稱';
    } else if (registerData.username.length < 2) {
      errors.username = '使用者名稱至少需要 2 個字元';
    } else if (registerData.username.length > 20) {
      errors.username = '使用者名稱不能超過 20 個字元';
    }

    if (!registerData.email) {
      errors.email = '請輸入 Email';
    } else if (!/\S+@\S+\.\S+/.test(registerData.email)) {
      errors.email = '請輸入有效的 Email';
    }

    if (!registerData.password) {
      errors.password = '請輸入密碼';
    } else if (registerData.password.length < 6) {
      errors.password = '密碼至少需要 6 個字元';
    } else if (!/^(?=.*[a-zA-Z])(?=.*\d)/.test(registerData.password)) {
      errors.password = '密碼必須包含英文字母和數字';
    }

    if (!registerData.confirmPassword) {
      errors.confirmPassword = '請再次輸入密碼';
    } else if (registerData.password !== registerData.confirmPassword) {
      errors.confirmPassword = '兩次輸入的密碼不一致';
    }

    if (!registerData.agreement) {
      errors.agreement = '請閱讀並同意服務條款';
    }

    setRegisterErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // 處理登入提交
  const handleLogin = async (e) => {
    e.preventDefault();
    if (!validateLoginForm()) return;

    setIsSubmitting(true);
    setErrorMessage('');

    const result = await login({
      email: loginData.email,
      password: loginData.password,
    });

    setIsSubmitting(false);

    if (result.success) {
      setSnackbar({ open: true, message: '登入成功！', severity: 'success' });
      navigate('/');
    } else {
      setErrorMessage(result.error || '登入失敗，請檢查帳號密碼');
    }
  };

  // 處理註冊提交
  const handleRegister = async (e) => {
    e.preventDefault();
    if (!validateRegisterForm()) return;

    setIsSubmitting(true);
    setErrorMessage('');

    const result = await register({
      username: registerData.username,
      email: registerData.email,
      password: registerData.password,
    });

    setIsSubmitting(false);

    if (result.success) {
      setSnackbar({ open: true, message: '註冊成功！', severity: 'success' });
      navigate('/');
    } else {
      setErrorMessage(result.error || '註冊失敗，請稍後再試');
    }
  };

  // 登入表單
  const LoginForm = (
    <form onSubmit={handleLogin}>
      {errorMessage && activeTab === 0 && (
        <Alert
          severity="error"
          onClose={() => setErrorMessage('')}
          sx={{ mb: 3 }}
        >
          {errorMessage}
        </Alert>
      )}

      <TextField
        fullWidth
        label="Email"
        name="email"
        type="email"
        value={loginData.email}
        onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
        error={!!loginErrors.email}
        helperText={loginErrors.email}
        disabled={isSubmitting}
        margin="normal"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <MdEmail />
            </InputAdornment>
          ),
        }}
        placeholder="請輸入您的 Email"
      />

      <TextField
        fullWidth
        label="密碼"
        name="password"
        type={showLoginPassword ? 'text' : 'password'}
        value={loginData.password}
        onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
        error={!!loginErrors.password}
        helperText={loginErrors.password}
        disabled={isSubmitting}
        margin="normal"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <MdLock />
            </InputAdornment>
          ),
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                onClick={() => setShowLoginPassword(!showLoginPassword)}
                edge="end"
              >
                {showLoginPassword ? <MdVisibilityOff /> : <MdVisibility />}
              </IconButton>
            </InputAdornment>
          ),
        }}
        placeholder="請輸入您的密碼"
      />

      <Box className={styles.formOptions} sx={{ mt: 1, mb: 2 }}>
        <FormControlLabel
          control={
            <Checkbox
              checked={loginData.remember}
              onChange={(e) => setLoginData({ ...loginData, remember: e.target.checked })}
              disabled={isSubmitting}
            />
          }
          label="記住我"
        />
        <Link
          component="button"
          type="button"
          onClick={() => setSnackbar({ open: true, message: '忘記密碼功能開發中', severity: 'info' })}
          sx={{ cursor: 'pointer' }}
        >
          忘記密碼？
        </Link>
      </Box>

      <Button
        type="submit"
        variant="contained"
        fullWidth
        size="large"
        disabled={isSubmitting}
        sx={{ mt: 2 }}
      >
        {isSubmitting ? '登入中...' : '登入'}
      </Button>
    </form>
  );

  // 註冊表單
  const RegisterForm = (
    <form onSubmit={handleRegister}>
      {errorMessage && activeTab === 1 && (
        <Alert
          severity="error"
          onClose={() => setErrorMessage('')}
          sx={{ mb: 3 }}
        >
          {errorMessage}
        </Alert>
      )}

      <TextField
        fullWidth
        label="使用者名稱"
        name="username"
        value={registerData.username}
        onChange={(e) => setRegisterData({ ...registerData, username: e.target.value })}
        error={!!registerErrors.username}
        helperText={registerErrors.username}
        disabled={isSubmitting}
        margin="normal"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <MdPerson />
            </InputAdornment>
          ),
        }}
        placeholder="請輸入使用者名稱"
      />

      <TextField
        fullWidth
        label="Email"
        name="email"
        type="email"
        value={registerData.email}
        onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
        error={!!registerErrors.email}
        helperText={registerErrors.email}
        disabled={isSubmitting}
        margin="normal"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <MdEmail />
            </InputAdornment>
          ),
        }}
        placeholder="請輸入您的 Email"
      />

      <TextField
        fullWidth
        label="密碼"
        name="password"
        type={showRegisterPassword ? 'text' : 'password'}
        value={registerData.password}
        onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
        error={!!registerErrors.password}
        helperText={registerErrors.password}
        disabled={isSubmitting}
        margin="normal"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <MdLock />
            </InputAdornment>
          ),
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                onClick={() => setShowRegisterPassword(!showRegisterPassword)}
                edge="end"
              >
                {showRegisterPassword ? <MdVisibilityOff /> : <MdVisibility />}
              </IconButton>
            </InputAdornment>
          ),
        }}
        placeholder="請輸入密碼（至少 6 位，需包含英文和數字）"
      />

      <TextField
        fullWidth
        label="確認密碼"
        name="confirmPassword"
        type={showConfirmPassword ? 'text' : 'password'}
        value={registerData.confirmPassword}
        onChange={(e) => setRegisterData({ ...registerData, confirmPassword: e.target.value })}
        error={!!registerErrors.confirmPassword}
        helperText={registerErrors.confirmPassword}
        disabled={isSubmitting}
        margin="normal"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <MdLock />
            </InputAdornment>
          ),
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                edge="end"
              >
                {showConfirmPassword ? <MdVisibilityOff /> : <MdVisibility />}
              </IconButton>
            </InputAdornment>
          ),
        }}
        placeholder="請再次輸入密碼"
      />

      <FormControlLabel
        control={
          <Checkbox
            checked={registerData.agreement}
            onChange={(e) => setRegisterData({ ...registerData, agreement: e.target.checked })}
            disabled={isSubmitting}
          />
        }
        label={
          <Typography variant="body2">
            我已閱讀並同意{' '}
            <Link
              component="button"
              type="button"
              onClick={() => setSnackbar({ open: true, message: '服務條款頁面開發中', severity: 'info' })}
            >
              服務條款
            </Link>{' '}
            和{' '}
            <Link
              component="button"
              type="button"
              onClick={() => setSnackbar({ open: true, message: '隱私權政策頁面開發中', severity: 'info' })}
            >
              隱私權政策
            </Link>
          </Typography>
        }
        sx={{ mt: 2, mb: 1, alignItems: 'flex-start' }}
      />
      {registerErrors.agreement && (
        <Typography color="error" variant="caption" sx={{ ml: 4, display: 'block', mt: -1 }}>
          {registerErrors.agreement}
        </Typography>
      )}

      <Button
        type="submit"
        variant="contained"
        fullWidth
        size="large"
        disabled={isSubmitting}
        sx={{ mt: 2 }}
      >
        {isSubmitting ? '註冊中...' : '註冊'}
      </Button>
    </form>
  );

  return (
    <div className={styles.loginPage}>
      <div className={styles.container}>
        <Card className={styles.loginCard}>
          <CardContent>
            <Box className={styles.header} sx={{ textAlign: 'center', mb: 3 }}>
              <Typography variant="h4" component="h2" gutterBottom>
                歡迎來到 MimiMart
              </Typography>
              <Typography variant="body1" color="text.secondary">
                請登入您的帳號或註冊新帳號
              </Typography>
            </Box>

            <Tabs
              value={activeTab}
              onChange={(event, newValue) => setActiveTab(newValue)}
              centered
              variant="fullWidth"
              sx={{ mb: 2 }}
            >
              <Tab label="登入" />
              <Tab label="註冊" />
            </Tabs>

            <TabPanel value={activeTab} index={0}>
              {LoginForm}
            </TabPanel>
            <TabPanel value={activeTab} index={1}>
              {RegisterForm}
            </TabPanel>
          </CardContent>
        </Card>
      </div>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert
          severity={snackbar.severity}
          onClose={() => setSnackbar({ ...snackbar, open: false })}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </div>
  );
};

export default Login;
