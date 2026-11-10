'use client';

import { usePathname } from 'next/navigation';
import { useAppState } from '@/context/AppStateContext.jsx';
import AppLayout, { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import './PageAccessGate.css';

// 목록과 상세만 공개한다. /jobs/me 같은 별도 경로를 포괄 허용하지 않는다.
export function isPublicServicePath(pathname) {
  return pathname === '/' || pathname === '/jobs'
    || /^\/jobs\/[0-9]+\/?$/.test(pathname ?? '')
    || /^\/threads\/[^/]+\/?$/.test(pathname ?? '');
}

// 서비스 셸은 유지하되 비로그인 상태에서는 보호된 화면을 마운트하지 않는다.
// 독립 이력서 편집 화면도 차단 시에는 공통 셸에서 로그인 방법을 안내한다.
export default function PageAccessGate({ children, standalone = false }) {
  const pathname = usePathname();
  const { authLoading, loggedIn } = useAppState();

  if (isPublicServicePath(pathname) || (!authLoading && loggedIn)) return children;

  const notice = <>
    <MainColumn>
      <section className="page-access" aria-labelledby="page-access-title">
        {authLoading || pathname === null ? <>
          <h1 id="page-access-title">로그인 상태 확인</h1>
          <p role="status">로그인 상태를 확인하고 있어요.</p>
        </> : <>
          <h1 id="page-access-title">로그인이 필요한 서비스입니다</h1>
          <p>로그인하고 이력서, 피드백과 다양한 서비스를 이용해 보세요.</p>
          <div className="page-access__actions">
            <Button to="/login">로그인</Button>
            <Button to="/" variant="outline">홈으로 이동</Button>
          </div>
        </>}
      </section>
    </MainColumn>
    <RightAside label="보조 정보" />
  </>;

  return standalone ? <AppLayout>{notice}</AppLayout> : notice;
}
