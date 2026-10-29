import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { JOBS } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import FollowButton from '../components/common/FollowButton.jsx';
import Icon from '../components/common/Icon.jsx';
import './JobDetailPage.css';

const MODES = [
  { id: 'base', title: '공통 이력서로 시작', desc: '학력 · 기술 · 프로젝트를 불러오고 요구 문항만 새로 작성', cta: '공통 이력서 불러와서 작성하기' },
  { id: 'blank', title: '처음부터 작성', desc: '빈 이력서에서 이 회사에 맞춰 전부 새로 작성', cta: '빈 이력서로 작성하기' }
];

export default function JobDetailPage() {
  const { id } = useParams();
  const job = JOBS.find((j) => String(j.id) === id && j.type === 'special') || JOBS[0];
  const [mode, setMode] = useState('base');
  const current = MODES.find((m) => m.id === mode);

  return (
    <>
      <MainColumn>
        <div className="page-head">
          <Link to="/jobs" className="icon-btn icon-btn--plain" aria-label="공고 목록으로"><Icon name="back" /></Link>
          <h1>공고</h1>
        </div>

        <div className="section" style={{ gap: 20 }}>
          <div className="row" style={{ gap: 12 }}>
            <Avatar initial={job.initial} color={job.av} size={56} company />
            <div className="grow"><div className="bold">{job.company}</div><div className="small muted">@{job.handle}</div></div>
            <FollowButton handle={job.handle} />
          </div>
          <div className="stack" style={{ gap: 10 }}>
            <div className="row-wrap" style={{ gap: 6 }}>
              <Badge tone="special" size="md">특화 공고</Badge>
              <Badge size="md">마감 {job.dday}</Badge>
            </div>
            <h2 className="job-detail__title">{job.title}</h2>
            <div className="small muted">{job.meta}</div>
          </div>
          <p style={{ lineHeight: 1.75 }}>{job.intro}</p>
          <div className="grid-2">
            <section className="job-detail__box"><h3>자격 요건</h3><ul>{job.requirements.map((r) => <li key={r}>{r}</li>)}</ul></section>
            <section className="job-detail__box"><h3>우대 사항</h3><ul>{job.preferred.map((r) => <li key={r}>{r}</li>)}</ul></section>
          </div>
        </div>

        <section className="section">
          <div className="row-wrap" style={{ justifyContent: 'space-between' }}>
            <h3 className="section-title">회사가 요구하는 문항</h3>
            <span className="small muted">특화 이력서에 함께 작성돼요</span>
          </div>
          {job.questions.map((q, i) => (
            <div key={q.text} className="req-item">
              <span className="req-item__num">{i + 1}</span>
              <div><div className="bold">{q.text}</div><div className="small muted">필수 · {q.limit}자 이내</div></div>
            </div>
          ))}
        </section>

        <section className="section">
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
          <div><div className="xs bold text-2">일반 공고</div><div className="text-2" style={{ fontSize: 14 }}>공통 이력서를 그대로 제출</div></div>
          <div><div className="xs bold" style={{ color: 'var(--special)' }}>특화 공고</div><div className="text-2" style={{ fontSize: 14 }}>회사 요구 문항을 담은 특화 이력서를 공고에서 바로 작성해 제출</div></div>
        </section>
      </RightAside>
    </>
  );
}
