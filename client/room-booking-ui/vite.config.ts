import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Allows devtunnel/ngrok-style hosts to reach the Vite dev server
    // when testing the app embedded in Microsoft Teams.
    allowedHosts: ['.devtunnels.ms'],
  },
})
