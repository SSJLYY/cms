import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    }
  },
  build: {
    // 设置chunk大小警告限制为2000kb
    chunkSizeWarningLimit: 2000,
    rollupOptions: {
      output: {
        // 简化的手动分割代码块
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
          'element-icons': ['@element-plus/icons-vue'],
          'echarts': ['echarts'],
          'axios': ['axios']
        },
        // 用于从入口点创建的块的打包输出格式
        entryFileNames: 'assets/[name]-[hash].js',
        // 用于命名代码拆分时创建的共享块的输出命名
        chunkFileNames: 'assets/[name]-[hash].js',
        // 用于输出静态资源的命名
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    },
    // 压缩选项
    minify: 'terser',
    terserOptions: {
      compress: {
        // 生产环境移除console
        drop_console: true,
        drop_debugger: true
      }
    }
  }
})
