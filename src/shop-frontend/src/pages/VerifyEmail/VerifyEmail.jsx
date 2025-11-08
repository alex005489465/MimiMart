/**
 * Email 驗證頁面
 * 處理會員 Email 驗證流程
 */
import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Stack,
} from '@mui/material';
import { MdCheckCircle, MdError, MdEmail } from 'react-icons/md';
import { authService } from '../../services/authService';
import useAuthStore from '../../stores/authStore';
import styles from './VerifyEmail.module.css';

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user, refreshUser } = useAuthStore();
  const [status, setStatus] = useState('verifying'); // verifying, success, error
  const [message, setMessage] = useState('');
  const [isResending, setIsResending] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);

  const token = searchParams.get('token');

  // 驗證 Email
  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('缺少驗證 Token');
      return;
    }

    const verifyEmailToken = async () => {
      try {
        const response = await authService.verifyEmail(token);

        if (response.success) {
          setStatus('success');
          setMessage('Email 驗證成功！');
          // 如果使用者已登入，重新整理資料
          if (user) {
            await refreshUser();
          }
        } else {
          setStatus('error');
          setMessage(response.message || '驗證失敗');
        }
      } catch (error) {
        setStatus('error');
        const errorMsg = error.response?.data?.message || '驗證失敗，請稍後再試';
        setMessage(errorMsg);
      }
    };

    verifyEmailToken();
  }, [token, user, refreshUser]);

  // 重新發送驗證郵件
  const handleResendEmail = async () => {
    if (!user?.email) {
      setMessage('無法取得 Email 地址，請重新登入');
      return;
    }

    setIsResending(true);
    try {
      const response = await authService.resendVerificationEmail(user.email);

      if (response.success) {
        setMessage('驗證郵件已重新發送，請檢查信箱');
        // 開始倒數計時 (60 秒)
        setResendCountdown(60);
      } else {
        setMessage(response.message || '發送失敗');
      }
    } catch (error) {
      setMessage(error.response?.data?.message || '發送失敗，請稍後再試');
    } finally {
      setIsResending(false);
    }
  };

  // 倒數計時
  useEffect(() => {
    if (resendCountdown > 0) {
      const timer = setTimeout(() => {
        setResendCountdown(resendCountdown - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [resendCountdown]);

  // 渲染內容
  const renderContent = () => {
    switch (status) {
      case 'verifying':
        return (
          <Stack spacing={3} alignItems="center">
            <CircularProgress size={60} />
            <Typography variant="h5">正在驗證...</Typography>
            <Typography color="text.secondary">
              請稍候，我們正在驗證您的 Email
            </Typography>
          </Stack>
        );

      case 'success':
        return (
          <Stack spacing={3} alignItems="center">
            <MdCheckCircle size={80} color="#4caf50" />
            <Typography variant="h5">驗證成功！</Typography>
            <Typography color="text.secondary">{message}</Typography>
            <Stack direction="row" spacing={2}>
              {user ? (
                <Button
                  variant="contained"
                  onClick={() => navigate('/member')}
                >
                  前往會員中心
                </Button>
              ) : (
                <Button
                  variant="contained"
                  onClick={() => navigate('/login')}
                >
                  前往登入
                </Button>
              )}
              <Button
                variant="outlined"
                onClick={() => navigate('/')}
              >
                返回首頁
              </Button>
            </Stack>
          </Stack>
        );

      case 'error':
        return (
          <Stack spacing={3} alignItems="center">
            <MdError size={80} color="#f44336" />
            <Typography variant="h5">驗證失敗</Typography>
            <Alert severity="error" sx={{ width: '100%' }}>
              {message}
            </Alert>
            <Typography color="text.secondary">
              驗證連結可能已過期或無效
            </Typography>

            {user && (
              <Button
                variant="contained"
                startIcon={<MdEmail />}
                onClick={handleResendEmail}
                disabled={isResending || resendCountdown > 0}
              >
                {isResending
                  ? '發送中...'
                  : resendCountdown > 0
                  ? `重新發送 (${resendCountdown}s)`
                  : '重新發送驗證郵件'}
              </Button>
            )}

            <Stack direction="row" spacing={2}>
              {user ? (
                <Button
                  variant="outlined"
                  onClick={() => navigate('/member')}
                >
                  前往會員中心
                </Button>
              ) : (
                <Button
                  variant="outlined"
                  onClick={() => navigate('/login')}
                >
                  前往登入
                </Button>
              )}
              <Button
                variant="text"
                onClick={() => navigate('/')}
              >
                返回首頁
              </Button>
            </Stack>
          </Stack>
        );

      default:
        return null;
    }
  };

  return (
    <div className={styles.verifyEmailPage}>
      <div className={styles.container}>
        <Card className={styles.verifyCard}>
          <CardContent sx={{ p: 4 }}>
            <Box sx={{ minHeight: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {renderContent()}
            </Box>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default VerifyEmail;
