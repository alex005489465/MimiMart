import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { ThemeProvider } from './context/ThemeContext'
import Home from './pages/Home/Home'

function App() {
  return (
    <ThemeProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Home />} />
          {/* 未來可以在這裡加入更多路由 */}
          {/* <Route path="/products" element={<Products />} /> */}
          {/* <Route path="/cart" element={<Cart />} /> */}
          {/* <Route path="/login" element={<Login />} /> */}
        </Routes>
      </Router>
    </ThemeProvider>
  )
}

export default App
