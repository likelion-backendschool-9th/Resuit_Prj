'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { getCommonResume } from '../services/resumes.js';
import Badge from '../components/common/Badge.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import TocLink from '../components/resume/TocLink.jsx';
import './ResumeEditPage.css';

const FIELD_LABEL = { FRONTEND: '프론트엔드', BACKEND: '백엔드' };

// 공통 이력서 항목. id는 섹션 위치, 이름은 목차와 제목에 쓴다 (docs/database-design.md 이력서 content)
const SECTIONS = [
  { id: 'basic', label: '기본 정보', filled: (c) => Object.values(c.basic || {}).some((v) => String(v ?? '').trim()) },
  { id: 'skills', label: '기술 스택', filled: (c) => (c.skills || []).length > 0 },
  { id: 'projects', label: '프로젝트', filled: (c) => (c.projects || []).length > 0 },
  { id: 'awards', label: '수상', filled: (c) => (c.awards || []).length > 0 },
  { id: 'introduction', label: '자기소개', filled: (c) => String(c.introduction ?? '').trim() !== '' }
];

const EMPTY = <span className="small muted">아직 작성하지 않았어요</span>;
const awardText = (a) => (typeof a === 'string' ? a : [a.title, a.date].filter(Boolean).join(' · '));

// 공통 이력서 편집 (WBS 3.6). 상세(3.8)로 불러와 섹션별로 고친 뒤 수정 API로 저장한다.
export default function CommonResumeEditPage({ id }) {
  const [resume, setResume] = useState(null);
  const [error, setError] = useState('');
  const [activeToc, setActiveToc] = useState('basic');

  useEffect(() => {
    if (!id || !/^\d+$/.test(id)) {
      setError('편집할 이력서를 찾을 수 없어요.');
      return undefined;
    }
    const controller = new AbortController();
    getCommonResume(id, controller.signal)
      .then((detail) => setResume({ ...detail, content: detail.content || {} }))
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); });
    return () => controller.abort();
  }, [id]);

  if (error || !resume) {
    return (
      <div className="resume-edit">
        <div className="empty" role={error ? 'alert' : 'status'} style={{ margin: 40 }}>
          {error ? <><p>{error}</p><Link href="/resumes" className="text-link">내 이력서로</Link></> : '공통 이력서를 불러오는 중이에요.'}
        </div>
      </div>
    );
  }

  const { content } = resume;
  const basic = content.basic || {};
  const done = SECTIONS.filter((s) => s.filled(content)).length;
  const toc = { active: activeToc, onPick: setActiveToc };

  return (
    <div className="resume-edit">
      <header className="resume-edit__bar">
        <div className="resume-edit__bar-in">
          <Link href="/resumes" className="icon-btn icon-btn--plain" aria-label="내 이력서로 돌아가기"><Icon name="back" /></Link>
          <div className="grow" style={{ flexBasis: 320 }}>
            <div className="row" style={{ gap: 6 }}>
              <Badge tone="accent" size="md">공통 이력서</Badge>
              {resume.field && <Badge tone="general" size="md">{FIELD_LABEL[resume.field] || resume.field}</Badge>}
            </div>
            <h1 className="resume-edit__title">{resume.title}</h1>
          </div>
          <div className="row"><ThemeToggle /></div>
        </div>
      </header>

      <div className="resume-edit__layout">
        <nav className="resume-edit__toc" aria-label="이력서 섹션">
          <div className="resume-edit__toc-group">이력서 항목</div>
          {SECTIONS.map((s) => <TocLink key={s.id} target={s.id} {...toc}>{s.label}</TocLink>)}
        </nav>

        <main className="resume-edit__form">
          <section id="basic" className="resume-block">
            <div className="resume-block__head"><h2>기본 정보</h2></div>
            {!SECTIONS[0].filled(content) ? EMPTY : (
              <div className="grid-3" style={{ fontSize: 14 }}>
                <div><div className="xs muted">이름</div><div style={{ fontWeight: 500 }}>{basic.name}</div></div>
                <div><div className="xs muted">학력</div><div style={{ fontWeight: 500 }}>{basic.school}</div></div>
                <div><div className="xs muted">GitHub</div><div style={{ fontWeight: 500 }}>{basic.github}</div></div>
              </div>
            )}
          </section>

          <section id="skills" className="resume-block">
            <div className="resume-block__head"><h2>기술 스택</h2></div>
            {!content.skills?.length ? EMPTY : (
              <div className="row-wrap">{content.skills.map((s) => <span key={s} className="resume-skill">{s}</span>)}</div>
            )}
          </section>

          <section id="projects" className="resume-block">
            <div className="resume-block__head"><h2>프로젝트</h2></div>
            {!content.projects?.length ? EMPTY : content.projects.map((p, i) => (
              <div key={p.id || i} className="resume-project">
                <span><span className="bold" style={{ display: 'block' }}>{p.title}</span><span className="small muted">{p.desc}</span></span>
              </div>
            ))}
          </section>

          <section id="awards" className="resume-block">
            <div className="resume-block__head"><h2>수상</h2></div>
            {!content.awards?.length ? EMPTY : (
              <ul className="stack" style={{ gap: 6, paddingLeft: 18, fontSize: 14 }}>{content.awards.map((a, i) => <li key={i}>{awardText(a)}</li>)}</ul>
            )}
          </section>

          <section id="introduction" className="resume-block">
            <div className="resume-block__head"><h2>자기소개</h2></div>
            {!SECTIONS[4].filled(content) ? EMPTY : <p style={{ fontSize: 14, whiteSpace: 'pre-wrap' }}>{content.introduction}</p>}
          </section>
        </main>

        <aside className="resume-edit__side">
          <section className="card">
            <h2 style={{ fontSize: 16 }}>작성 현황</h2>
            <div className="progress"><div style={{ width: `${(done / SECTIONS.length) * 100}%` }} /></div>
            <div className="text-2" style={{ fontSize: 14 }}>이력서 항목 {done} / {SECTIONS.length} 작성</div>
          </section>
        </aside>
      </div>
    </div>
  );
}
