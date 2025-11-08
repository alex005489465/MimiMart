/**
 * 忘記密碼頁面
 * 處理密碼重設申請流程
 */
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Alert,
  InputAdornment,
  Stack,
} from '@mui/material';
import { MdEmail, MdArrowBack } from 'react-icons/md';
import { authService } from '../../services/authService';
import styles from './ForgotPassword.module.css';

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // 表單驗證
  const validateForm = () => {
    const newErrors = {};

    if (!email) {
      newErrors.email = '請輸入 Email';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = '請輸入有效的 Email';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 處理提交
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const response = await authService.forgotPassword(email);

      if (response.success) {
        setSubmitSuccess(true);
      } else {
        setErrorMessage(response.message || '申請失敗，請稍後再試');
      }
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || '申請失敗，請稍後再試'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.forgotPasswordPage}>
      <div className={styles.container}>
        <Card className={styles.forgotPasswordCard}>
          <CardContent sx={{ p: 4 }}>
            {!submitSuccess ? (
              <>
                <Box sx={{ mb: 3, textAlign: 'center' }}>
                  <Typography variant="h4" component="h2" gutterBottom>
                    忘記密碼
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    請輸入您的 Email，我們將發送密碼重設連結給您
                  </Typography>
                </Box>

                {errorMessage && (
                  <Alert severity="error" sx={{ mb: 3 }} onClose={() => setErrorMessage('')}>
                    {errorMessage}
                  </Alert>
                )}

                <Box component="form" onSubmit={handleSubmit}>
                  <TextField
                    fullWidth
                    label="Email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    error={!!errors.email}
                    helperText={errors.email}
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
                    autoFocus
                  />

                  <Button
                    type="submit"
                    variant="contained"
                    fullWidth
                    size="large"
                    disabled={isSubmitting}
                    sx={{ mt: 3 }}
                  >
                    {isSubmitting ? '發送中...' : '發送重設連結'}
                  </Button>

                  <Box sx={{ mt: 2, textAlign: 'center' }}>
                    <Button
                      component={Link}
                      to="/login"
                      startIcon={<MdArrowBack />}
                      sx={{ textTransform: 'none' }}
                    >
                      返回登入
                    </Button>
                  </Box>
                </Box>
              </>
            ) : (
              <Stack spacing={3} alignItems="center" sx={{ py: 4 }}>
                <Box
                  sx={{
                    width: 80,
                    height: 80,
                    borderRadius: '50%',
                    backgroundColor: 'success.light',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <MdEmail size={40} color="#fff" />
                </Box>

                <Typography variant="h5">郵件已發送</Typography>

                <Alert severity="success" sx={{ width: '100%' }}>
                  密碼重設郵件已發送至 <strong>{email}</strong>
                  <br />
                  請至信箱查收並點擊連結以重設密碼
                </Alert>

                <Typography variant="body2" color="text.secondary">
                  郵件有效期限為 30 分鐘
                </Typography>

                <Stack direction="row" spacing={2}>
                  <Button variant="contained" onClick={() => navigate('/login')}>
                    前往登入
                  </Button>
                  <Button variant="outlined" onClick={() => setSubmitSuccess(false)}>
                    重新發送
                  </Button>
                </Stack>
              </Stack>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default ForgotPassword;
