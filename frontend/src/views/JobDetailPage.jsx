'use client';
import { useCallback } from 'react';
import Link from 'next/link';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Icon from '@/components/common/Icon.jsx';
import JobHead from '@/components/jobs/JobHead.jsx';
import JobOwnerActions from '@/components/jobs/JobOwnerActions.jsx';
import JobRequestState from '@/components/jobs/JobRequestState.jsx';
import useJobResource from '@/components/jobs/useJobResource.js';
import { useAppState } from '@/context/AppStateContext.jsx';
import { getJob } from '@/services/jobs.js';
import '@/components/jobs/Jobs.css';

export default function JobDetailPage({ id, initialJob = null }) {
  const { loggedIn, me } = useAppState();
  const load = useCallback((signal) => getJob(id, { signal }), [id]);
  const state = useJobResource(load, initialJob);
  return <><MainColumn>
    <div className="page-head"><Link href="/jobs" className="icon-btn icon-btn--plain" aria-label="공고 목록으로"><Icon name="back" /></Link><h1>채용 공고</h1></div>
    <JobRequestState {...state} />
    {state.data && !state.loading && !state.error && <><JobHead job={state.data} />
      {loggedIn && me.handle === state.data.company.username && <JobOwnerActions job={state.data} />}
      <section className="section"><h3>지원 안내</h3><p className="muted">온라인 지원 기능은 준비 중입니다.</p></section>
    </>}
  </MainColumn><RightAside showReviews label="공고 안내"><section className="card"><h2>공고 살펴보기</h2><p className="small muted">일반 공고는 공통 이력서로, 특화 공고는 회사 요구 문항을 담은 이력서로 지원하는 방식입니다.</p></section></RightAside></>;
}
