'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import './ResumeEditPage.css';

function TocLink({ target, active, onPick, children }) {
  const go = () => {
    onPick(target);
    document.getElementById(target)?.scrollIntoView({ behavior: 'smooth' });
  };
  return (
    <button type="button" className={`resume-edit__toc-link${active === target ? ' is-active' : ''}`} aria-current={active === target ? 'true' : undefined} onClick={go}>
      {children}
    </button>
  );
}

// 특화 이력서 작성. jobId / mode는 공고 API 연동 후 사용한다.
export default function ResumeEditPage({ jobId, mode }) {
  const router = useRouter();
  const blank = mode !== 'base';
  const questions = [];
  const [answers, setAnswers] = useState(() => questions.map(() => ''));
  const [savedLabel, setSavedLabel] = useState('자동 저장됨 · 방금 전');
  const [activeToc, setActiveToc] = useState('basic');

  const done = answers.filter((a) => a.trim()).length;
  const total = Math.max(questions.length, 1);
  const toc = { active: activeToc, onPick: setActiveToc };
  const backHref = jobId ? `/jobs/${jobId}` : '/jobs';

  return (
    <div className="resume-edit">
      <header className="resume-edit__bar">
        <div className="resume-edit__bar-in">
          <Link href={backHref} className="icon-btn icon-btn--plain" aria-label="공고로 돌아가기"><Icon name="back" /></Link>
          <div className="grow" style={{ flexBasis: 320 }}>
            <div className="row" style={{ gap: 6 }}><Badge tone="special" size="md">특화 이력서</Badge><span className="small muted">{savedLabel}</span></div>
            <h1 className="resume-edit__title">특화 이력서 작성</h1>
          </div>
          <div className="row">
            <ThemeToggle />
            <Button variant="outline" onClick={() => setSavedLabel('임시 저장됨 · 방금 전')}>임시 저장</Button>
            <Button onClick={() => router.push('/resumes')}>제출하기</Button>
          </div>
        </div>
      </header>

      <div className="resume-edit__layout">
        <nav className="resume-edit__toc" aria-label="이력서 섹션">
          <div className="resume-edit__toc-group">{blank ? '기본 항목' : '공통 이력서에서 불러옴'}</div>
          <TocLink target="basic" {...toc}>기본 정보</TocLink>
          <TocLink target="skills" {...toc}>기술 스택</TocLink>
          <TocLink target="projects" {...toc}>프로젝트</TocLink>
          <div className="resume-edit__toc-group is-req">회사 요구 문항</div>
          {questions.length === 0
            ? <span className="small muted" style={{ padding: '4px 12px' }}>문항 없음</span>
            : questions.map((_, i) => <TocLink key={i} target={`q${i + 1}`} {...toc}>문항 {i + 1}</TocLink>)}
        </nav>

        <main className="resume-edit__form">
          {blank && <div className="resume-edit__note">빈 이력서로 시작했어요. 아래 항목을 이 회사에 맞춰 새로 작성해 주세요.</div>}
          {!blank && <div className="resume-edit__note">공통 이력서 데이터를 불러올 API가 아직 없어요. 항목을 직접 작성해 주세요.</div>}

          <section id="basic" className="resume-block">
            <div className="resume-block__head">
              <h2>기본 정보</h2>
            </div>
            <span className="small muted">아직 작성하지 않았어요</span>
          </section>

          <section id="skills" className="resume-block">
            <div className="resume-block__head"><h2>기술 스택</h2></div>
            <span className="small muted">아직 작성하지 않았어요</span>
          </section>

          <section id="projects" className="resume-block">
            <div className="resume-block__head"><h2>프로젝트</h2></div>
            <span className="small muted">아직 작성하지 않았어요</span>
          </section>

          {questions.map((q, i) => (
            <section key={q.text} id={`q${i + 1}`} className="resume-block resume-block--req">
              <span style={{ alignSelf: 'flex-start' }}><Badge tone="special" size="md">회사 요구 문항 {i + 1} · 필수</Badge></span>
              <label htmlFor={`q${i + 1}-input`} className="bold" style={{ fontSize: 16 }}>{q.text}</label>
              <textarea
                id={`q${i + 1}-input`} className="textarea" rows={6} maxLength={q.limit}
                placeholder={q.placeholder}
                value={answers[i]} onChange={(e) => setAnswers((a) => a.map((x, j) => (j === i ? e.target.value : x)))}
              />
              <div className="resume-block__foot small muted">
                <span>{q.hint}</span>
                <span>{answers[i].length} / {q.limit}자</span>
              </div>
            </section>
          ))}
        </main>

        <aside className="resume-edit__side">
          <section className="card">
            <h2 style={{ fontSize: 16 }}>작성 현황</h2>
            <div className="progress"><div style={{ width: `${(done / total) * 100}%` }} /></div>
            <div className="text-2" style={{ fontSize: 14 }}>필수 문항 {done} / {questions.length} 작성</div>
          </section>
          <section className="card" style={{ gap: 10 }}>
            <h2 style={{ fontSize: 16 }}>제출 전 피드백 받기</h2>
            <p className="text-2" style={{ fontSize: 14 }}>이 이력서를 회사명을 가린 채 피드에 올려 다른 사용자에게 피드백을 받을 수 있어요.</p>
            <Button variant="accent-outline" to="/feedback">피드백 게시판에 요청</Button>
          </section>
        </aside>
      </div>
    </div>
  );
}
