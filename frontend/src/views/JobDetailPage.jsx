'use client';

import { useState } from 'react';
import Link from 'next/link';
import { JOBS } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import JobHead from '../components/jobs/JobHead.jsx';
import GeneralJobDetail from '../components/jobs/GeneralJobDetail.jsx';
import './JobDetailPage.css';

const MODES = [
  { id: 'base', title: '공통 이력서로 시작', desc: '학력 · 기술 · 프로젝트를 불러오고 요구 문항만 새로 작성', cta: '공통 이력서 불러와서 작성하기' },
  { id: 'blank', title: '처음부터 작성', desc: '빈 이력서에서 이 회사에 맞춰 전부 새로 작성', cta: '빈 이력서로 작성하기' }
];

// 특화 공고 상세: 요구 문항을 확인하고 특화 이력서 작성으로 넘어간다.
function SpecialJobDetail({ job }) {
  const [mode, setMode] = useState('base');
  const current = MODES.find((m) => m.id === mode);

  return (
    <>
      <MainColumn>
        <div className="page-head">
          <Link href="/jobs" className="icon-btn icon-btn--plain" aria-label="공고 목록으로"><Icon name="back" /></Link>
          <h1>공고</h1>
        </div>

        <JobHead job={job} />

        <section className="section" style={{ padding: 24 }}>
          <div className="row-wrap" style={{ justifyContent: 'space-between', alignItems: 'baseline', gap: 12 }}>
            <h3 className="section-title">회사가 요구하는 문항</h3>
            <span className="small muted">특화 이력서에 함께 작성돼요</span>
          </div>
          {job.questions.map((q, i) => (
            <div key={q.text} className="req-item">
              <span className="req-item__num">{i + 1}</span>
              <div className="stack" style={{ gap: 4 }}><div className="bold">{q.text}</div><div className="small muted">필수 · {q.limit}자 이내</div></div>
            </div>
          ))}
        </section>

        <section className="section" style={{ padding: 24 }}>
          <h3 className="section-title">특화 이력서 작성 시작</h3>
          <div className="grid-2" role="radiogroup" aria-label="작성 방식">
            {MODES.map((m) => (
              <button key={m.id} type="button" role="radio" aria-checked={mode === m.id} className={`choice${mode === m.id ? ' is-on' : ''}`} onClick={() => setMode(m.id)}>
                <span className="choice__title">{m.title}</span>
                <span className="choice__desc">{m.desc}</span>
              </button>
            ))}
          </div>
          <Button size="lg" block to={`/resumes/edit?job=${job.id}&mode=${mode}`}>{current.cta}</Button>
        </section>
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>지원 방식</h2>
          <div className="stack" style={{ gap: 4 }}><span className="xs bold text-2">일반 공고</span><span className="text-2" style={{ fontSize: 14 }}>공통 이력서를 그대로 제출</span></div>
          <div className="stack" style={{ gap: 4 }}><span className="xs bold" style={{ color: 'var(--special)' }}>특화 공고</span><span className="text-2" style={{ fontSize: 14 }}>회사 요구 문항을 담은 특화 이력서를 공고에서 바로 작성해 제출</span></div>
        </section>
        {job.otherJobs && (
          <section className="card" style={{ gap: 10 }}>
            <h2>이 회사의 다른 공고</h2>
            {job.otherJobs.map((o) => <Link key={o.label} href={o.to} style={{ textDecoration: 'none', fontSize: 14, fontWeight: 500 }}>{o.label}</Link>)}
          </section>
        )}
      </RightAside>
    </>
  );
}

// /jobs/[id] : 공고 종류에 맞는 상세 화면을 보여 준다.
export default function JobDetailPage({ id }) {
  const job = JOBS.find((j) => String(j.id) === id) || JOBS[0];
  return job.type === 'special'
    ? <SpecialJobDetail key={job.id} job={job} />
    : <GeneralJobDetail key={job.id} job={job} />;
}
