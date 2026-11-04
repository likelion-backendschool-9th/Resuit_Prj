'use client';

import { useEffect, useState } from 'react';
import { useAppState } from '@/context/AppStateContext.jsx';
import Button from '@/components/common/Button.jsx';
import ThemeToggle from '@/components/common/ThemeToggle.jsx';
import './OnboardingPage.css';

const NAMES = { google: 'Google', github: 'GitHub', naver: '네이버' };

export default function OnboardingPage() {
  const { loggedIn, me, authProviders, authLoading, authError } = useAppState();
  const [loginError, setLoginError] = useState(false);
  useEffect(() => {
    const url = new URL(window.location.href);
    if (loggedIn) {
      setLoginError(false);
      if (url.searchParams.has('error')) {
        url.searchParams.delete('error');
        window.history.replaceState(null, '', `${url.pathname}${url.search}${url.hash}`);
      }
    } else {
      setLoginError(url.searchParams.get('error') === 'oauth');
    }
  }, [loggedIn]);
  return (
    <div className="onboarding"><div className="onboarding__wrap">
      <div className="onboarding__theme"><ThemeToggle withLabel /></div>
      <div className="onboarding__hero"><div className="onboarding__brand">Resuit</div>
        <p className="onboarding__tagline">개발 활동이 곧 이력서가 되는 곳.<br />계정으로 로그인하고 피드백을 나눠 보세요.</p>
      </div>
      <section className="onboarding__panel">
        <h1 className="onboarding__title--lg">{loggedIn ? `${me.name}님, 반가워요` : 'Resuit 시작하기'}</h1>
        <p className="onboarding__desc">Google, GitHub, 네이버 계정으로 로그인할 수 있어요.</p>
        {!loggedIn && !authLoading && (loginError || authError) && <p role="alert">{authError || '로그인을 완료하지 못했어요. 다시 시도해 주세요.'}</p>}
        {authLoading ? <p role="status">로그인 서비스를 확인하고 있어요.</p>
          : loggedIn ? <Button to="/" block>피드로 가기</Button>
          : authProviders.map((provider) => provider.enabled
            ? <a key={provider.id} href={provider.loginUrl} className="btn btn--outline btn--lg btn--block">{NAMES[provider.id]} 계정으로 계속하기</a>
            : <Button key={provider.id} variant="outline" size="lg" block disabled>{NAMES[provider.id]} 로그인 준비 중</Button>)}
        {!loggedIn && <p className="xs muted">로그인 제공자의 화면에서 인증합니다. 비밀번호는 Resuit에 입력하지 않아요.</p>}
      </section>
      {!loggedIn && <Button to="/" variant="ghost">로그인 없이 둘러보기</Button>}
    </div></div>
  );
}
