// Vite 配置文件：开发和构建参数先放这里
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
  port: 5173,
  host: '0.0.0.0',
  cors: true,
  allowedHosts: true,
  hmr: { clientPort: 443 },
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 你的 Spring Boot 端口，确认是8080吗？
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '/api/v1')  // 如果后端路径是 /api/v1/xxx，就这样写；如果后端是 /api/xxx，就去掉 rewrite 或改成 path
    }
  }
}
})