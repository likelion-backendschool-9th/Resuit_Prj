'use client';

import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { useAppState } from '@/context/AppStateContext.jsx';
import CompanyJobWorkspace from '@/components/jobs/CompanyJobWorkspace.jsx';
import './CompanyPostPage.css';
import '@/components/jobs/Jobs.css';

export default function CompanyPostPage({ jobId = null, draft = false }) {
  const { authLoading, loggedIn, me } = useAppState();
  return <><MainColumn>
    <div className="page-title"><h1>{jobId ? draft ? '임시저장 공고 편집' : '채용 공고 수정' : '새 채용 공고'}</h1>{jobId && <Button to="/company/post" variant="outline" size="sm">새 공고 작성</Button>}</div>
    <p className="page-desc">기업 계정으로 채용 정보를 작성하고 게시하세요.</p>
    {authLoading ? <p className="empty" role="status">로그인 상태를 확인하고 있어요.</p> : !loggedIn ? <div className="section"><p>공고를 작성하려면 기업 계정으로 로그인해 주세요.</p><Button to="/onboarding">로그인</Button></div>
      : <CompanyJobWorkspace key={me.id} id={jobId} draft={draft} owner={me.handle} />}
  </MainColumn><RightAside label="공고 작성 안내"><section className="card"><h2>공고 작성 안내</h2><p className="small muted">일반 공고는 공통 이력서를 받습니다. 특화 공고에는 지원자가 답할 요구 문항을 추가하세요.</p><p className="small muted">임시저장한 공고는 공개되지 않습니다. 작성이 끝나면 공고 게시를 눌러 주세요.</p><Button to="/jobs" variant="outline">공고 목록</Button></section></RightAside></>;
}
