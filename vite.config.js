import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// base: './' 로 두면 빌드 결과(dist)를 GitHub Pages 등 하위 경로에 올려도 동작한다.
export default defineConfig({
  plugins: [react()],
  base: './'
});
