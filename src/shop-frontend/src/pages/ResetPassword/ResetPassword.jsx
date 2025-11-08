/**
 * 重設密碼頁面
 * 處理密碼重設流程
 */
import { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Alert,
  InputAdornment,
  IconButton,
  Stack,
  LinearProgress,
} from '@mui/material';
import { MdLock, MdVisibility, MdVisibilityOff, MdCheckCircle } from 'react-icons/md';
import { authService } from '../../services/authService';
import styles from './ResetPassword.module.css';

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // 密碼強度計算
  const getPasswordStrength = (password) => {
    if (!password) return 0;
    let strength = 0;
    if (password.length >= 6) strength += 25;
    if (password.length >= 8) strength += 25;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength += 25;
    if (/\d/.test(password)) strength += 25;
    return strength;
  };

  const passwordStrength = getPasswordStrength(formData.newPassword);

  // 表單驗證
  const validateForm = () => {
    const newErrors = {};

    if (!formData.newPassword) {
      newErrors.newPassword = '請輸入新密碼';
    } else if (formData.newPassword.length < 6) {
      newErrors.newPassword = '密碼至少需要 6 個字元';
    } else if (!/^(?=.*[a-zA-Z])(?=.*\d)/.test(formData.newPassword)) {
      newErrors.newPassword = '密碼必須包含英文字母和數字';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '請再次輸入密碼';
    } else if (formData.newPassword !== formData.confirmPassword) {
      newErrors.confirmPassword = '兩次輸入的密碼不一致';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 處理提交
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      setErrorMessage('缺少重設 Token');
      return;
    }

    if (!validateForm()) return;

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const response = await authService.resetPassword(
        token,
        formData.newPassword,
        formData.confirmPassword
      );

      if (response.success) {
        setSubmitSuccess(true);
      } else {
        setErrorMessage(response.message || '重設失敗，請稍後再試');
      }
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || '重設失敗，請稍後再試'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  // 如果沒有 token，顯示錯誤
  if (!token) {
    return (
      <div className={styles.resetPasswordPage}>
        <div className={styles.container}>
          <Card className={styles.resetPasswordCard}>
            <CardContent sx={{ p: 4 }}>
              <Stack spacing={3} alignItems="center">
                <Alert severity="error" sx={{ width: '100%' }}>
                  缺少重設 Token，請檢查郵件中的連結是否完整
                </Alert>
                <Button variant="contained" onClick={() => navigate('/forgot-password')}>
                  重新申請密碼重設
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.resetPasswordPage}>
      <div className={styles.container}>
        <Card className={styles.resetPasswordCard}>
          <CardContent sx={{ p: 4 }}>
            {!submitSuccess ? (
              <>
                <Box sx={{ mb: 3, textAlign: 'center' }}>
                  <Typography variant="h4" component="h2" gutterBottom>
                    重設密碼
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    請輸入您的新密碼
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
                    label="新密碼"
                    type={showNewPassword ? 'text' : 'password'}
                    value={formData.newPassword}
                    onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                    error={!!errors.newPassword}
                    helperText={errors.newPassword}
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
                            onClick={() => setShowNewPassword(!showNewPassword)}
                            edge="end"
                          >
                            {showNewPassword ? <MdVisibilityOff /> : <MdVisibility />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    }}
                    placeholder="請輸入新密碼（至少 6 位，需包含英文和數字）"
                  />

                  {/* 密碼強度指示器 */}
                  {formData.newPassword && (
                    <Box sx={{ mt: 1, mb: 2 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                        <Typography variant="caption" color="text.secondary">
                          密碼強度
                        </Typography>
                        <Typography
                          variant="caption"
                          color={
                            passwordStrength >= 75
                              ? 'success.main'
                              : passwordStrength >= 50
                              ? 'warning.main'
                              : 'error.main'
                          }
                        >
                          {passwordStrength >= 75 ? '強' : passwordStrength >= 50 ? '中' : '弱'}
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={passwordStrength}
                        sx={{
                          height: 6,
                          borderRadius: 3,
                          backgroundColor: 'grey.200',
                          '& .MuiLinearProgress-bar': {
                            borderRadius: 3,
                            backgroundColor:
                              passwordStrength >= 75
                                ? 'success.main'
                                : passwordStrength >= 50
                                ? 'warning.main'
                                : 'error.main',
                          },
                        }}
                      />
                    </Box>
                  )}

                  <TextField
                    fullWidth
                    label="確認新密碼"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    error={!!errors.confirmPassword}
                    helperText={errors.confirmPassword}
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
                    placeholder="請再次輸入新密碼"
                  />

                  <Button
                    type="submit"
                    variant="contained"
                    fullWidth
                    size="large"
                    disabled={isSubmitting}
                    sx={{ mt: 3 }}
                  >
                    {isSubmitting ? '重設中...' : '重設密碼'}
                  </Button>
                </Box>
              </>
            ) : (
              <Stack spacing={3} alignItems="center" sx={{ py: 4 }}>
                <MdCheckCircle size={80} color="#4caf50" />
                <Typography variant="h5">密碼重設成功！</Typography>
                <Alert severity="success" sx={{ width: '100%' }}>
                  您的密碼已成功重設，請使用新密碼登入
                </Alert>
                <Button variant="contained" fullWidth onClick={() => navigate('/login')}>
                  前往登入
                </Button>
              </Stack>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default ResetPassword;
