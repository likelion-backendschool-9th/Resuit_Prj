'use client';

import LoginPage from './LoginPage.jsx';

// 기존 링크와 OAuth 실패 리다이렉트에서도 같은 로그인 화면을 제공한다.
export default function OnboardingPage() {
  return <LoginPage />;
}
