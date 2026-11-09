'use client';

import { useCallback, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Tabs from '@/components/common/Tabs.jsx';
import Button from '@/components/common/Button.jsx';
import JobCard from '@/components/jobs/JobCard.jsx';
import JobRequestState from '@/components/jobs/JobRequestState.jsx';
import useJobResource from '@/components/jobs/useJobResource.js';
import { getJobs, toJobCard } from '@/services/jobs.js';
import '@/components/jobs/Jobs.css';

export default function JobsPage({ initialJobs = null }) {
  const [tab, setTab] = useState('all');
  const [page, setPage] = useState(0);
  const load = useCallback((signal) => getJobs({ type: tab, page, signal }), [tab, page]);
  const state = useJobResource(load, tab === 'all' && page === 0 ? initialJobs : null);
  return <>
    <MainColumn>
      <div className="page-title"><h1>채용 공고</h1><span className="small muted">최신순</span></div>
      <p className="page-desc">나의 경험과 맞는 다음 기회를 찾아보세요.</p>
      <Tabs items={[{ id: 'all', label: '전체' }, { id: 'general', label: '일반 공고' }, { id: 'special', label: '특화 공고' }]} value={tab} onChange={(value) => { setTab(value); setPage(0); }} label="공고 종류" />
      <JobRequestState {...state} />
      {!state.loading && !state.error && state.data && <div className="jobs-content">
        <p className="small muted">공고 {state.data.totalElements}개</p>
        {state.data.content.length ? state.data.content.map((job) => <JobCard key={job.id} job={toJobCard(job)} />) : <p className="empty">아직 등록된 공고가 없어요.</p>}
        <nav className="jobs-pagination" aria-label="공고 페이지">
          <Button variant="outline" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</Button>
          <span>{page + 1} / {Math.max(1, state.data.totalPages)}</span>
          <Button variant="outline" disabled={state.data.last} onClick={() => setPage(page + 1)}>다음</Button>
        </nav>
      </div>}
    </MainColumn>
    <RightAside showReviews label="채용 안내"><section className="card"><h2>새로운 동료를 찾고 있나요?</h2><p className="small muted">기업 계정으로 공고를 작성하고 임시저장한 내용을 이어서 편집하세요.</p><Button to="/company/post" variant="outline">공고 작성·임시저장 관리</Button></section></RightAside>
  </>;
}
