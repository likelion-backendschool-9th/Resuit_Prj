import Script from 'next/script';
import { ThemeProvider } from '@/context/ThemeContext.jsx';
import { AppStateProvider } from '@/context/AppStateContext.jsx';
import '@/styles/tokens.css';
import '@/styles/global.css';

export const metadata = {
  title: { default: 'Resuit', template: '%s | Resuit' },
  description: '개발 활동이 곧 이력서가 되는 곳. GitHub과 블로그를 연결하고 피드백을 받아 보세요.'
};

// hydration 전에 저장된 테마를 적용한다.
const THEME_SCRIPT = `(function () {
  var t = 'light';
  try {
    var saved = localStorage.getItem('resuit-theme');
    if (saved) t = saved;
    else if (matchMedia('(prefers-color-scheme: dark)').matches) t = 'dark';
  } catch (e) {}
  document.documentElement.setAttribute('data-theme', t);
})();`;

export default function RootLayout({ children }) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap" />
        <Script id="resuit-theme-init" strategy="beforeInteractive" dangerouslySetInnerHTML={{ __html: THEME_SCRIPT }} />
      </head>
      <body>
        <ThemeProvider>
          <AppStateProvider>{children}</AppStateProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
