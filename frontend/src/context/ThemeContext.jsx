'use client';

import { createContext, useCallback, useContext, useEffect, useState } from 'react';

const ThemeContext = createContext(null);

// 라이트/다크 모드. <html data-theme> 를 바꾸면 tokens.css 의 변수가 통째로 바뀐다.
// 첫 화면의 테마는 app/layout.jsx 의 인라인 스크립트가 정하고, 여기서는 그 값을 읽어 온다.
export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  useEffect(() => {
    setTheme(document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light');
  }, []);

  const toggleTheme = useCallback(() => {
    const next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    try { localStorage.setItem('resuit-theme', next); } catch { /* 저장 불가 환경은 무시 */ }
    setTheme(next);
  }, []);

  return <ThemeContext.Provider value={{ theme, toggleTheme }}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  return useContext(ThemeContext);
}
