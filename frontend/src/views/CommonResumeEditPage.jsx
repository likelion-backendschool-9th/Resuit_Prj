'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { getCommonResume } from '../services/resumes.js';
import Badge from '../components/common/Badge.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import { ChipRadioGroup } from '../components/common/Chip.jsx';
import TocLink from '../components/resume/TocLink.jsx';
import { TextField, TextAreaField } from '../components/resume/ResumeFields.jsx';
import SkillsEditor from '../components/resume/SkillsEditor.jsx';
import './ResumeEditPage.css';

const FIELD_LABEL = { FRONTEND: '프론트엔드', BACKEND: '백엔드' };
// 분야를 비우면('') 수정 API가 분야를 지운다
const FIELD_OPTIONS = [{ id: 'FRONTEND', label: '프론트엔드' }, { id: 'BACKEND', label: '백엔드' }, { id: '', label: '정하지 않음' }];
const MAX_TITLE = 40;
const MAX_INTRODUCTION = 2000;
const BASIC_FIELDS = [
  { key: 'name', label: '이름', max: 30, placeholder: '예: 이서연' },
  { key: 'school', label: '학력', max: 100, placeholder: '예: OO대학교 컴퓨터공학과' },
  { key: 'github', label: 'GitHub', max: 200, placeholder: '예: github.com/seoyeon-dev' }
];

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
  // draft: 화면에서 고치는 중인 { title, field, content }
  const [draft, setDraft] = useState(null);
  const [error, setError] = useState('');
  const [activeToc, setActiveToc] = useState('info');

  useEffect(() => {
    if (!id || !/^\d+$/.test(id)) {
      setError('편집할 이력서를 찾을 수 없어요.');
      return undefined;
    }
    const controller = new AbortController();
    getCommonResume(id, controller.signal)
      .then((detail) => setDraft({ title: detail.title, field: detail.field || '', content: detail.content || {} }))
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); });
    return () => controller.abort();
  }, [id]);

  if (error || !draft) {
    return (
      <div className="resume-edit">
        <div className="empty" role={error ? 'alert' : 'status'} style={{ margin: 40 }}>
          {error ? <><p>{error}</p><Link href="/resumes" className="text-link">내 이력서로</Link></> : '공통 이력서를 불러오는 중이에요.'}
        </div>
      </div>
    );
  }

  const { content } = draft;
  const basic = content.basic || {};
  const done = SECTIONS.filter((s) => s.filled(content)).length;
  const toc = { active: activeToc, onPick: setActiveToc };
  const change = (key, value) => setDraft((d) => ({ ...d, [key]: value }));
  // 화면이 다루지 않는 content 값(프로젝트 근거 등)은 그대로 두고 바꾼 항목만 덮는다
  const changeContent = (key, value) => setDraft((d) => ({ ...d, content: { ...d.content, [key]: value } }));
  const changeBasic = (key, value) => changeContent('basic', { ...basic, [key]: value });

  return (
    <div className="resume-edit">
      <header className="resume-edit__bar">
        <div className="resume-edit__bar-in">
          <Link href="/resumes" className="icon-btn icon-btn--plain" aria-label="내 이력서로 돌아가기"><Icon name="back" /></Link>
          <div className="grow" style={{ flexBasis: 320 }}>
            <div className="row" style={{ gap: 6 }}>
              <Badge tone="accent" size="md">공통 이력서</Badge>
              {draft.field && <Badge tone="general" size="md">{FIELD_LABEL[draft.field] || draft.field}</Badge>}
            </div>
            <h1 className="resume-edit__title">{draft.title.trim() || '이름 없는 이력서'}</h1>
          </div>
          <div className="row"><ThemeToggle /></div>
        </div>
      </header>

      <div className="resume-edit__layout">
        <nav className="resume-edit__toc" aria-label="이력서 섹션">
          <TocLink target="info" {...toc}>이력서 이름 · 분야</TocLink>
          <div className="resume-edit__toc-group">이력서 항목</div>
          {SECTIONS.map((s) => <TocLink key={s.id} target={s.id} {...toc}>{s.label}</TocLink>)}
        </nav>

        <main className="resume-edit__form">
          <section id="info" className="resume-block">
            <div className="resume-block__head"><h2>이력서 이름 · 분야</h2></div>
            <TextField
              id="resume-title" label="이력서 이름" max={MAX_TITLE} placeholder="예: 백엔드 지원용"
              hint="지원할 때와 프로필에 이 이름으로 보여요" value={draft.title} onChange={(v) => change('title', v)}
            />
            <div className="field resume-field">
              <span className="label" style={{ fontSize: 14 }}>분야</span>
              <ChipRadioGroup options={FIELD_OPTIONS} value={draft.field} onChange={(v) => change('field', v)} label="이력서 분야" />
              <span className="xs muted">피드백 게시판의 분야 필터에 쓰여요</span>
            </div>
          </section>

          <section id="basic" className="resume-block">
            <div className="resume-block__head"><h2>기본 정보</h2></div>
            <div className="resume-fields">
              {BASIC_FIELDS.map((f) => (
                <TextField key={f.key} id={`basic-${f.key}`} label={f.label} max={f.max} placeholder={f.placeholder} value={basic[f.key]} onChange={(v) => changeBasic(f.key, v)} />
              ))}
            </div>
          </section>

          <section id="skills" className="resume-block">
            <div className="resume-block__head"><h2>기술 스택</h2></div>
            <SkillsEditor skills={content.skills || []} onChange={(v) => changeContent('skills', v)} />
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
            <TextAreaField
              id="introduction-input" label="자기소개" max={MAX_INTRODUCTION} rows={8}
              placeholder="어떤 개발자인지, 무엇을 잘하고 어떻게 일하는지 적어 주세요."
              value={content.introduction} onChange={(v) => changeContent('introduction', v)}
            />
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
