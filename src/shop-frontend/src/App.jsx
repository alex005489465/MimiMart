import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { ThemeProvider } from './context/ThemeContext'
import { AuthProvider } from './context/AuthContext'
import Home from './pages/Home/Home'
import Login from './pages/Login/Login'
import Register from './pages/Register/Register'
import Member from './pages/Member/Member'
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute'

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/member"
              element={
                <ProtectedRoute>
                  <Member />
                </ProtectedRoute>
              }
            />
            {/* 未來可以在這裡加入更多路由 */}
            {/* <Route path="/products" element={<Products />} /> */}
            {/* <Route path="/cart" element={<Cart />} /> */}
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  )
}

export default App
