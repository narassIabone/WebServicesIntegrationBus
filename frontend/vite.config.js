import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
      }
    }
  },
  build: {
    outDir: 'D:/Programms/IntelliJ IDEA 2025.1.3/projects/WebServicesIntegrationBus/backend/src/main/resources/static',
    emptyOutDir: false,
  }
})