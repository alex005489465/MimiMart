/**
 * MimiMart 主應用元件
 * 整合 Ant Design ConfigProvider、Zustand 狀態管理、路由系統
 */
import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ConfigProvider, theme as antTheme, App as AntApp } from 'antd';
import zhTW from 'antd/locale/zh_TW';

// Stores
import useAuthStore from './stores/authStore';
import useUIStore from './stores/uiStore';

// Theme
import { lightTheme, darkTheme } from './styles/theme';

// Layout
import AppLayout from './components/layout/AppLayout';

// Pages
import Home from './pages/Home/Home';
import Login from './pages/Login/Login';
import Member from './pages/Member/Member';
import Cart from './pages/Cart/Cart';
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
  const themeConfig = currentTheme === 'dark' ? darkTheme : lightTheme;

  return (
    <ConfigProvider
      locale={zhTW}
      theme={{
        ...themeConfig,
        algorithm:
          currentTheme === 'dark'
            ? antTheme.darkAlgorithm
            : antTheme.defaultAlgorithm,
      }}
    >
      <AntApp>
        <Router>
          <Routes>
            {/* 使用 Layout 的路由 */}
            <Route element={<AppLayout />}>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route
                path="/member/*"
                element={
                  <ProtectedRoute>
                    <Member />
                  </ProtectedRoute>
                }
              />

              {/* 購物車 */}
              <Route path="/cart" element={<Cart />} />

              {/* 商品相關路由（待實作） */}
              {/* <Route path="/products" element={<Products />} /> */}
              {/* <Route path="/products/:id" element={<ProductDetail />} /> */}
              {/* <Route path="/checkout" element={<Checkout />} /> */}

              {/* 其他頁面（待實作） */}
              {/* <Route path="/promotions" element={<Promotions />} /> */}
              {/* <Route path="/new-arrivals" element={<NewArrivals />} /> */}

              {/* 404 頁面 */}
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </Router>
      </AntApp>
    </ConfigProvider>
  );
}

export default App;
