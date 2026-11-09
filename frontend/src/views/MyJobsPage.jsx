'use client';

import { useCallback, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import JobCard from '@/components/jobs/JobCard.jsx';
import JobRequestState from '@/components/jobs/JobRequestState.jsx';
import useJobResource from '@/components/jobs/useJobResource.js';
import { useAppState } from '@/context/AppStateContext.jsx';
import { getMyJobs, toJobCard } from '@/services/jobs.js';
import '@/components/jobs/Jobs.css';

function MyJobsList() {
  const [page, setPage] = useState(0);
  const load = useCallback((signal) => getMyJobs(page, { signal }), [page]);
  const state = useJobResource(load);
  return <>
    <JobRequestState {...state} />
    {state.error && <div className="section"><Button to="/profile/verification" variant="outline">기업 인증 확인</Button></div>}
    {!state.loading && !state.error && state.data && <div className="jobs-content">
      <p className="small muted">전체 {state.data.totalElements}개 · 최근 수정순</p>
      {state.data.content.length ? state.data.content.map((job) => {
        const draft = job.status === 'DRAFT';
        const card = toJobCard(job);
        return <JobCard key={job.id} job={{ ...card, dday: draft ? '임시저장' : job.status === 'CLOSED' ? '마감' : `게시 중 · ${card.dday}` }}
          actionTo={draft ? `/company/post?draft=${job.id}` : `/jobs/${job.id}`}
          actionLabel={draft ? '이어서 작성' : '상세·관리'} />;
      }) : <div className="empty"><p>아직 작성한 공고가 없어요.</p><Button to="/company/post">첫 공고 작성</Button></div>}
      <nav className="jobs-pagination" aria-label="내 공고 페이지">
        <Button variant="outline" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</Button>
        <span>{page + 1} / {Math.max(1, state.data.totalPages)}</span>
        <Button variant="outline" disabled={state.data.last} onClick={() => setPage(page + 1)}>다음</Button>
      </nav>
    </div>}
  </>;
}

export default function MyJobsPage() {
  const { loggedIn, authLoading, me } = useAppState();
  return <>
    <MainColumn>
      <div className="page-title"><h1>내가 작성한 공고</h1><Button to="/company/post" size="sm">공고 작성</Button></div>
      <p className="page-desc">게시한 공고와 임시저장한 공고를 한곳에서 관리하세요.</p>
      {authLoading ? <p className="empty" role="status">로그인 상태를 확인하고 있어요.</p>
        : !loggedIn ? <div className="section"><p>기업 계정으로 로그인하면 내가 작성한 공고를 볼 수 있어요.</p><Button to="/login">로그인</Button></div>
          : <MyJobsList key={me.id} />}
    </MainColumn>
    <RightAside showReviews label="공고 관리 안내"><section className="card"><h2>내 공고 관리</h2><p className="small muted">임시저장 공고는 나에게만 표시됩니다. 게시한 공고의 상세 화면에서 수정·삭제할 수 있어요.</p><Button to="/jobs" variant="outline">전체 채용 공고</Button></section></RightAside>
  </>;
}
