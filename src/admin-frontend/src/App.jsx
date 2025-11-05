import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ThemeProvider } from './context/ThemeContext'
import Layout from './components/Layout/Layout'
import Login from './pages/Login/Login'
import Dashboard from './pages/Dashboard/Dashboard'
import BannerList from './pages/Banners/BannerList'
import BannerForm from './pages/Banners/BannerForm'
import './App.css'

function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          {/* 登入頁面 - 不使用 Layout */}
          <Route path="/" element={<Login />} />

          {/* 後台頁面 - 使用共用 Layout（Header + Sidebar） */}
          <Route element={<Layout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/banners" element={<BannerList />} />
            <Route path="/banners/new" element={<BannerForm />} />
            <Route path="/banners/edit/:id" element={<BannerForm />} />
          </Route>

          {/* 404 導向登入頁 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  )
}

export default App
