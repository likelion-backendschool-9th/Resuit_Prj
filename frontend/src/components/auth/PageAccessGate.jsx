'use client';

import { useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useAppState } from '@/context/AppStateContext.jsx';
import AppLayout, { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import './PageAccessGate.css';

// 목록과 상세만 공개한다. /jobs/me 같은 별도 경로를 포괄 허용하지 않는다.
export function isPublicServicePath(pathname) {
  return pathname === '/' || pathname === '/jobs'
    || /^\/jobs\/[0-9]+\/?$/.test(pathname ?? '')
    || /^\/threads\/[^/]+\/?$/.test(pathname ?? '');
}

// 서비스 셸은 유지하되 비로그인 상태에서는 보호된 화면을 마운트하지 않는다.
// 독립 이력서 편집 화면도 차단 시에는 로그인 페이지로 바로 보낸다.
export default function PageAccessGate({ children, standalone = false }) {
  const pathname = usePathname();
  const router = useRouter();
  const { authLoading, loggedIn } = useAppState();
  const publicPath = isPublicServicePath(pathname);
  const allowed = publicPath || (!authLoading && loggedIn);

  useEffect(() => {
    if (publicPath || authLoading || loggedIn || pathname === null) return;
    router.replace('/login');
  }, [publicPath, authLoading, loggedIn, pathname, router]);

  if (allowed) return children;

  const notice = (
    <>
      <MainColumn>
        <section className="page-access" aria-labelledby="page-access-title">
          <h1 id="page-access-title">로그인 상태 확인</h1>
          <p role="status">
            {authLoading || pathname === null
              ? '로그인 상태를 확인하고 있어요.'
              : '로그인 페이지로 이동하고 있어요.'}
          </p>
        </section>
      </MainColumn>
      <RightAside label="보조 정보" />
    </>
  );

  return standalone ? <AppLayout>{notice}</AppLayout> : notice;
}
