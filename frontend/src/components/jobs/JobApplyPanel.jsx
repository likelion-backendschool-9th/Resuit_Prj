'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '@/context/AppStateContext.jsx';
import { getMyApplication, cancelApplication } from '@/services/jobApplications.js';
import { getAllResumes } from '@/services/resumes.js';
import Button from '@/components/common/Button.jsx';
import Icon from '@/components/common/Icon.jsx';
import GeneralApply from './GeneralApply.jsx';

// 서울 기준 오늘(YYYY-MM-DD). 마감일 당일까지 지원할 수 있다
const today = () => new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Seoul' }).format(new Date());
const timeText = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '');

// 공고 상세의 지원 영역. 내 지원 상태와 이력서 목록을 불러와 상태에 맞는 안내와 버튼을 보여 준다.
export default function JobApplyPanel({ job }) {
  const { authLoading, loggedIn, me } = useAppState();
  const owner = loggedIn && me.handle === job.company?.username;
  const [state, setState] = useState({ loading: true, error: '', application: null, resumes: null });
  const [retry, setRetry] = useState(0);

  const load = useCallback((signal) => Promise.all([getMyApplication(job.id, signal), getAllResumes(signal)])
    .then(([application, resumes]) => ({ application, resumes })), [job.id]);

  useEffect(() => {
    if (!loggedIn || owner) return undefined;
    const controller = new AbortController();
    setState((s) => ({ ...s, loading: true, error: '' }));
    load(controller.signal)
      .then((data) => setState({ loading: false, error: '', ...data }))
      .catch((err) => { if (!controller.signal.aborted) setState((s) => ({ ...s, loading: false, error: err.message })); });
    return () => controller.abort();
  }, [loggedIn, owner, load, retry]);

  // 공고를 올린 회사 본인에게는 지원 영역을 보여 주지 않는다
  if (owner) return null;

  let body;
  if (authLoading) body = <p className="muted" role="status">로그인 상태를 확인하고 있어요.</p>;
  else if (!loggedIn) body = <LoginPrompt />;
  else if (state.loading) body = <p className="muted" role="status">지원 정보를 불러오는 중이에요.</p>;
  else if (state.error) body = <div role="alert" className="stack" style={{ gap: 8 }}><p>{state.error}</p><Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>;
  else if (state.application.applied) body = <Applied job={job} application={state.application} resumes={state.resumes} onCanceled={() => setRetry((v) => v + 1)} />;
  else if (job.status === 'CLOSED' || (job.deadline && job.deadline < today())) body = <p className="muted">마감된 공고예요. 지금은 지원할 수 없어요.</p>;
  else if (job.type === 'GENERAL') body = <GeneralApply job={job} commonResumes={state.resumes.commonResumes} onApplied={() => setRetry((v) => v + 1)} />;
  else body = <p className="muted">아직 지원하지 않았어요.</p>;

  return (
    <section className="section job-apply">
      <h3>지원하기</h3>
      {body}
    </section>
  );
}

function LoginPrompt() {
  return (
    <div className="row-wrap" style={{ gap: 12, justifyContent: 'space-between' }}>
      <p className="muted">로그인하면 이 공고에 지원할 수 있어요.</p>
      <Button to="/login">로그인하고 지원하기</Button>
    </div>
  );
}

// 지원한 상태: 지원 일시와 제출한 이력서를 보여 주고 지원을 취소할 수 있다
function Applied({ job, application, resumes, onCanceled }) {
  const [confirming, setConfirming] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const common = application.resumeType === 'COMMON';
  // 특화 이력서는 따로 이름이 없고 공고로 구분한다
  const resumeName = common ? resumes.commonResumes.find((r) => r.id === application.resumeId)?.title || '공통 이력서 보기' : '이 공고용 특화 이력서';
  const resumeHref = common ? `/resumes/common/edit?id=${application.resumeId}` : `/resumes/edit?id=${application.resumeId}`;

  async function cancel() {
    setBusy(true);
    setError('');
    try {
      await cancelApplication(job.id);
      onCanceled();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  return (
    <div className="stack" style={{ gap: 14 }}>
      <div className="job-applied">
        <span className="job-applied__icon"><Icon name="check" size={22} /></span>
        <strong style={{ fontSize: 17 }}>이 공고에 지원했어요</strong>
      </div>
      <div className="job-applied__rows">
        <span className="muted">지원 일시</span><span>{timeText(application.appliedAt)}</span>
        <span className="muted">제출한 이력서</span>
        <span>{common ? '공통 이력서' : '특화 이력서'} · <Link href={resumeHref} className="text-link">{resumeName}</Link></span>
      </div>
      {confirming ? (
        <div className="row-wrap" style={{ gap: 8 }}>
          <span className="small">지원을 취소할까요? 공고가 모집 중이면 다시 지원할 수 있어요.</span>
          <Button variant="outline" onClick={cancel} disabled={busy}>{busy ? '취소하는 중…' : '지원 취소'}</Button>
          <Button variant="ghost" onClick={() => setConfirming(false)} disabled={busy}>그대로 두기</Button>
        </div>
      ) : (
        <div className="row-wrap" style={{ gap: 8 }}>
          <Button to="/resumes" variant="outline">내 이력서 보기</Button>
          <button type="button" className="job-applied__cancel" onClick={() => setConfirming(true)}>지원 취소</button>
        </div>
      )}
      {error && <p className="small" role="alert">{error}</p>}
    </div>
  );
}
