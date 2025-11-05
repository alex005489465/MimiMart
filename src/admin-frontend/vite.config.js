import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5174,
    watch: {
      usePolling: true
    },
    proxy: {
      '/api': {
        target: 'http://mimimart-java:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy, options) => {
          proxy.on('proxyRes', (proxyRes, req, res) => {
            // 手動添加 CORS 標頭
            proxyRes.headers['Access-Control-Allow-Origin'] = '*'
            proxyRes.headers['Access-Control-Allow-Methods'] = 'GET, POST, PUT, DELETE, PATCH, OPTIONS'
            proxyRes.headers['Access-Control-Allow-Headers'] = 'Content-Type, Authorization'
            proxyRes.headers['Access-Control-Allow-Credentials'] = 'true'
          })
        }
      }
    }
  }
})
