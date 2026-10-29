import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { HashRouter } from 'react-router-dom';
import App from './App.jsx';
import { ThemeProvider } from './context/ThemeContext.jsx';
import { AppStateProvider } from './context/AppStateContext.jsx';
import './styles/tokens.css';
import './styles/global.css';

// HashRouter: 서버 설정 없이 정적 호스팅(GitHub Pages 등)에서도 새로고침이 동작한다.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider>
      <AppStateProvider>
        <HashRouter>
          <App />
        </HashRouter>
      </AppStateProvider>
    </ThemeProvider>
  </StrictMode>
);
