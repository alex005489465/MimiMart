/**
 * 受保護路由元件
 * 檢查使用者是否已登入,未登入則導向登入頁面
 */
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  // 載入中顯示 Loading
  if (loading) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: 'calc(100vh - 80px)',
        }}
      >
        <div
          style={{
            fontSize: '1.5rem',
            color: 'var(--text-secondary)',
          }}
        >
          載入中...
        </div>
      </div>
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
