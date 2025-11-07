/**
 * 受保護路由元件
 * 檢查使用者是否已登入，未登入則導向登入頁面
 * 使用 Zustand 狀態管理和 MUI CircularProgress
 */
import { Navigate } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import useAuthStore from '../../stores/authStore';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuthStore();

  // 載入中顯示 MUI CircularProgress
  if (isLoading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: 'calc(100vh - 64px - 300px)', // 減去 header 和 footer 高度
          padding: '48px 0',
        }}
      >
        <CircularProgress size={60} />
      </Box>
    );
  }

  // 未登入導向登入頁面
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // 已登入顯示受保護的內容
  return children;
};

export default ProtectedRoute;
