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
import ItemListEditor from '../components/resume/ItemListEditor.jsx';
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

const MAX_PROJECTS = 20;
const MAX_AWARDS = 20;
// 특화 이력서가 프로젝트를 고를 때 id로 구분하므로 새 프로젝트에도 id를 붙인다
const newProject = () => ({ id: `p-${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`, title: '', desc: '' });
const newAward = () => ({ title: '', date: '' });

// 예전 데이터의 문자열 수상은 { title, date } 모양으로 맞춘다
const normalize = (content) => ({
  ...content,
  ...(Array.isArray(content.awards) && { awards: content.awards.map((a) => (typeof a === 'string' ? { title: a, date: '' } : a)) })
});

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
      .then((detail) => setDraft({ title: detail.title, field: detail.field || '', content: normalize(detail.content || {}) }))
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
            <ItemListEditor
              items={content.projects || []} onChange={(v) => changeContent('projects', v)}
              createItem={newProject} itemLabel="프로젝트" max={MAX_PROJECTS} emptyText="아직 작성한 프로젝트가 없어요"
              renderItem={(p, update, i) => (
                <>
                  <TextField id={`project-${i}-title`} label="프로젝트 이름" max={100} placeholder="예: 스터디 매칭 웹앱" value={p.title} onChange={(v) => update({ title: v })} />
                  <TextAreaField id={`project-${i}-desc`} label="프로젝트 설명" max={1000} rows={3} placeholder="사용한 기술, 팀 규모, 맡은 일과 결과를 적어 주세요." value={p.desc} onChange={(v) => update({ desc: v })} />
                  {p.evidence?.length > 0 && (
                    <div className="row-wrap" style={{ gap: 6 }}>
                      <span className="xs bold muted">근거</span>
                      {p.evidence.map((ev) => {
                        const type = String(ev.type).toLowerCase();
                        return <span key={ev.label} className={`evidence-chip evidence-chip--${type}`}><Icon name={type === 'repo' ? 'repo' : 'rss'} size={14} />{ev.label}</span>;
                      })}
                    </div>
                  )}
                </>
              )}
            />
          </section>

          <section id="awards" className="resume-block">
            <div className="resume-block__head"><h2>수상</h2></div>
            <ItemListEditor
              items={content.awards || []} onChange={(v) => changeContent('awards', v)}
              createItem={newAward} itemLabel="수상" max={MAX_AWARDS} emptyText="아직 작성한 수상 이력이 없어요"
              renderItem={(a, update, i) => (
                <div className="resume-fields">
                  <TextField id={`award-${i}-title`} label="수상 이름" max={100} placeholder="예: 교내 해커톤 우수상" value={a.title} onChange={(v) => update({ title: v })} />
                  <TextField id={`award-${i}-date`} label="받은 날" max={20} placeholder="예: 2025-11" value={a.date} onChange={(v) => update({ date: v })} />
                </div>
              )}
            />
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
