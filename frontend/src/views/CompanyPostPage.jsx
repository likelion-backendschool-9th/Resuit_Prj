'use client';

import { useState } from 'react';
import Avatar from '@/components/common/Avatar.jsx';
import Badge from '@/components/common/Badge.jsx';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { useAppState } from '@/context/AppStateContext.jsx';
import CompanyJobWorkspace from '@/components/jobs/CompanyJobWorkspace.jsx';
import './CompanyPostPage.css';
import '@/components/jobs/Jobs.css';

export default function CompanyPostPage({ jobId = null, draft = false }) {
  const { authLoading, loggedIn, me } = useAppState();
  const [preview, setPreview] = useState(null);
  const special = preview?.type === 'SPECIAL';
  return <><MainColumn>
    <div className="page-title company-post__head">
      <h1>{jobId ? draft ? '임시저장 공고 편집' : '채용 공고 수정' : '새 채용 공고'}</h1>
      <div className="row-wrap">
        {loggedIn && <span className="small muted">{me.name} 계정으로 게시</span>}
        {jobId && <Button to="/company/post" variant="outline" size="sm">새 공고 작성</Button>}
      </div>
    </div>
    {authLoading ? <p className="empty" role="status">로그인 상태를 확인하고 있어요.</p> : !loggedIn ? <div className="section"><p>공고를 작성하려면 기업 계정으로 로그인해 주세요.</p><Button to="/onboarding">로그인</Button></div>
      : <CompanyJobWorkspace key={me.id} id={jobId} draft={draft} owner={me.handle} onPreviewChange={setPreview} />}
  </MainColumn>
    <RightAside showReviews label="리뷰 추천 및 보조 정보">
      <h2 className="company-post__preview-title">피드 미리보기</h2>
      <div className="card company-post__preview">
        <div className="row"><Avatar initial={loggedIn ? me.initial : '?'} color="co2" size={36} company src={loggedIn ? me.profileImage || undefined : undefined} /><strong>{loggedIn ? me.name : '회사 이름'}</strong></div>
        <span className="company-post__preview-badge"><Badge tone={special ? 'special' : 'general'} size="md">{special ? `특화 공고 · 요구 문항 ${preview.questions.length}개` : '일반 공고'}</Badge></span>
        <div className="bold">{preview?.title || '[공고 제목]'}</div>
        <div className="small muted">{special ? '지원자는 공고에서 특화 이력서를 작성해 제출합니다.' : '지원자는 공통 이력서로 바로 지원합니다.'}</div>
      </div>
    </RightAside>
  </>;
}
