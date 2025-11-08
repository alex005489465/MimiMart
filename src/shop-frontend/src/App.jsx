/**
 * MimiMart 主應用元件
 * 整合 MUI ThemeProvider、Zustand 狀態管理、路由系統
 */
import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';

// Stores
import useAuthStore from './stores/authStore';
import useUIStore from './stores/uiStore';

// Theme
import { lightTheme, darkTheme } from './styles/muiTheme';

// Layout
import AppLayout from './components/layout/AppLayout';

// Pages
import Home from './pages/Home/Home';
import Login from './pages/Login/Login';
import Member from './pages/Member/Member';
import Cart from './pages/Cart/Cart';
import Checkout from './pages/Checkout/Checkout';
import Products from './pages/Products/Products';
import ProductDetail from './pages/ProductDetail/ProductDetail';
import VerifyEmail from './pages/VerifyEmail/VerifyEmail';
import ForgotPassword from './pages/ForgotPassword/ForgotPassword';
import ResetPassword from './pages/ResetPassword/ResetPassword';
import NotFound from './pages/NotFound/NotFound';
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';

function App() {
  const currentTheme = useUIStore((state) => state.theme);
  const initialize = useAuthStore((state) => state.initialize);

  // 初始化認證狀態
  useEffect(() => {
    initialize();
  }, [initialize]);

  // 選擇主題配置
  const theme = currentTheme === 'dark' ? darkTheme : lightTheme;

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Routes>
          {/* 使用 Layout 的路由 */}
          <Route element={<AppLayout />}>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/verify-email" element={<VerifyEmail />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route
              path="/member/*"
              element={
                <ProtectedRoute>
                  <Member />
                </ProtectedRoute>
              }
            />

            {/* 購物車與結帳 */}
            <Route path="/cart" element={<Cart />} />
            <Route path="/checkout" element={<Checkout />} />

            {/* 商品相關路由 */}
            <Route path="/products" element={<Products />} />
            <Route path="/products/:id" element={<ProductDetail />} />

            {/* 其他頁面（待實作） */}
            {/* <Route path="/promotions" element={<Promotions />} /> */}
            {/* <Route path="/new-arrivals" element={<NewArrivals />} /> */}

            {/* 404 頁面 */}
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;
