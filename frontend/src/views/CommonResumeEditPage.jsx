'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getCommonResume, updateCommonResume } from '../services/resumes.js';
import Button from '../components/common/Button.jsx';
import Badge from '../components/common/Badge.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import { ChipRadioGroup } from '../components/common/Chip.jsx';
import TocLink from '../components/resume/TocLink.jsx';
import { TextField, TextAreaField } from '../components/resume/ResumeFields.jsx';
import SkillsEditor from '../components/resume/SkillsEditor.jsx';
import BasicInfoFields from '../components/resume/BasicInfoFields.jsx';
import PhotoField from '../components/resume/PhotoField.jsx';
import ProjectsEditor from '../components/resume/ProjectsEditor.jsx';
import AwardsEditor from '../components/resume/AwardsEditor.jsx';
import { normalizeContent, cleanContent } from '../components/resume/resumeContent.js';
import './ResumeEditPage.css';

const FIELD_LABEL = { FRONTEND: '프론트엔드', BACKEND: '백엔드' };
// 분야를 비우면('') 수정 API가 분야를 지운다
const FIELD_OPTIONS = [{ id: 'FRONTEND', label: '프론트엔드' }, { id: 'BACKEND', label: '백엔드' }, { id: '', label: '정하지 않음' }];
const MAX_TITLE = 100;
const MAX_INTRODUCTION = 2000;

// 공통 이력서 항목. id는 섹션 위치, 이름은 목차와 제목에 쓴다 (docs/database-design.md 이력서 content)
const SECTIONS = [
  { id: 'basic', label: '기본 정보', filled: (c) => Object.values(c.basic || {}).some((v) => String(v ?? '').trim()) },
  { id: 'skills', label: '기술 스택', filled: (c) => (c.skills || []).length > 0 },
  { id: 'projects', label: '프로젝트', filled: (c) => (c.projects || []).length > 0 },
  { id: 'awards', label: '수상', filled: (c) => (c.awards || []).length > 0 },
  { id: 'introduction', label: '자기소개', filled: (c) => String(c.introduction ?? '').trim() !== '' }
];

// 서버 응답을 화면에서 고치는 모양({ title, field, content })으로 바꾼다
const fromDetail = (detail) => ({ title: detail.title, field: detail.field || '', content: normalizeContent(detail.content || {}), photoUrl: detail.photoUrl || '' });

// 저장할 값: 이름과 content의 앞뒤 공백을 지우고 빈 프로젝트·수상은 뺀다
// photoUrl: ''이면 사진 없음, tmpImage/...면 서버가 정식 폴더로 옮겨 저장한다.
const toPayload = ({ title, field, content, photoUrl }) => ({ title: title.trim(), field, content: cleanContent(content), photoUrl: photoUrl || '' });

// 저장 시각을 "방금 전", "3분 전"처럼 보여 준다
function agoText(time) {
  const minutes = Math.floor((Date.now() - time) / 60000);
  if (minutes < 1) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  return `${Math.floor(minutes / 60)}시간 전`;
}

// 공통 이력서 편집 (WBS 3.6). 상세(3.8)로 불러와 섹션별로 고친 뒤 수정 API로 저장한다.
export default function CommonResumeEditPage({ id }) {
  const router = useRouter();
  // draft: 화면에서 고치는 중인 { title, field, content }
  const [draft, setDraft] = useState(null);
  // baseline: 마지막으로 불러오거나 저장한 값. draft와 다르면 저장하지 않은 변경이 있다
  const [baseline, setBaseline] = useState('');
  const [error, setError] = useState('');
  const [alertText, setAlertText] = useState('');
  const [saving, setSaving] = useState(false);
  const [savedAt, setSavedAt] = useState(null);
  const [justSaved, setJustSaved] = useState(false);
  const [, setTick] = useState(0);
  const [activeToc, setActiveToc] = useState('info');

  const show = (next) => {
    setDraft(next);
    setBaseline(JSON.stringify(toPayload(next)));
  };

  useEffect(() => {
    if (!id || !/^\d+$/.test(id)) {
      setError('편집할 이력서를 찾을 수 없어요.');
      return undefined;
    }
    const controller = new AbortController();
    getCommonResume(id, controller.signal)
      .then((detail) => show(fromDetail(detail)))
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); });
    return () => controller.abort();
  }, [id]);

  const payload = draft && toPayload(draft);
  const dirty = !!payload && JSON.stringify(payload) !== baseline;

  // 저장하지 않고 새로고침하거나 창을 닫으면 브라우저가 한 번 묻는다
  useEffect(() => {
    if (!dirty) return undefined;
    const warn = (e) => { e.preventDefault(); e.returnValue = ''; };
    window.addEventListener('beforeunload', warn);
    return () => window.removeEventListener('beforeunload', warn);
  }, [dirty]);

  // "저장됨 · n분 전"이 시간에 맞게 바뀌도록 30초마다 다시 그린다
  useEffect(() => {
    if (!savedAt) return undefined;
    const timer = setInterval(() => setTick((t) => t + 1), 30000);
    return () => clearInterval(timer);
  }, [savedAt]);

  // 저장 버튼의 "저장됨" 표시는 잠깐만 보여 준다
  useEffect(() => {
    if (!justSaved) return undefined;
    const timer = setTimeout(() => setJustSaved(false), 1500);
    return () => clearTimeout(timer);
  }, [justSaved]);

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
  const done = SECTIONS.filter((s) => s.filled(payload.content)).length;
  // 저장에 성공하면 true
  async function save() {
    if (!dirty) return true;
    if (!payload.title) {
      setAlertText('이력서 이름을 입력해 주세요.');
      document.getElementById('resume-title')?.focus();
      return false;
    }
    setSaving(true);
    setAlertText('');
    try {
      show(fromDetail(await updateCommonResume(id, payload)));
      setSavedAt(Date.now());
      setJustSaved(true);
      return true;
    } catch (err) {
      setAlertText(err.message);
      return false;
    } finally {
      setSaving(false);
    }
  }

  // 바뀐 게 있으면 저장한 뒤, 없으면 바로 이력서 탭으로 간다. 저장했으면 이력서 탭이 안내를 띄운다
  async function saveAndLeave() {
    const changed = dirty;
    if (!(await save())) return;
    router.push(changed ? `/resumes?saved=${id}` : '/resumes');
  }

  const confirmLeave = (e) => {
    if (dirty && !window.confirm('저장하지 않은 내용이 있어요. 저장하지 않고 나갈까요?')) e.preventDefault();
  };
  const toc = { active: activeToc, onPick: setActiveToc };
  // 다시 고치기 시작하면 이전 오류 안내는 지운다
  const change = (key, value) => { setAlertText(''); setDraft((d) => ({ ...d, [key]: value })); };
  // 화면이 다루지 않는 content 값(프로젝트 근거 등)은 그대로 두고 바꾼 항목만 덮는다
  const changeContent = (key, value) => { setAlertText(''); setDraft((d) => ({ ...d, content: { ...d.content, [key]: value } })); };

  return (
    <div className="resume-edit">
      <header className="resume-edit__bar">
        <div className="resume-edit__bar-in">
          <Link href="/resumes" className="icon-btn icon-btn--plain" aria-label="내 이력서로 돌아가기" onClick={confirmLeave}><Icon name="back" /></Link>
          <div className="grow" style={{ flexBasis: 320 }}>
            <div className="row" style={{ gap: 6 }}>
              <Badge tone="accent" size="md">공통 이력서</Badge>
              {draft.field && <Badge tone="general" size="md">{FIELD_LABEL[draft.field] || draft.field}</Badge>}
            </div>
            <h1 className="resume-edit__title">{draft.title.trim() || '이름 없는 이력서'}</h1>
          </div>
          <div className="row resume-edit__actions">
            <ThemeToggle />
            {/* 저장 상태는 저장 버튼 바로 옆에 둔다. 자리 폭을 잡아 두어 글씨가 바뀌어도 버튼이 밀리지 않게 한다 */}
            <span className="resume-edit__status-slot" role={alertText ? 'alert' : 'status'}>
              {alertText ? <span className="small resume-edit__status is-alert">{alertText}</span>
                : dirty ? <span className="small resume-edit__status is-dirty">저장하지 않은 변경 있음</span>
                : savedAt ? <span className="small resume-edit__status is-saved"><Icon name="check" size={14} />저장됨 · {agoText(savedAt)}</span>
                : null}
            </span>
            <Button variant="outline" onClick={save} disabled={saving || (!dirty && !justSaved)} className={`resume-edit__save${justSaved ? ' is-saved' : ''}`}>
              {saving ? '저장 중…' : justSaved ? <><Icon name="check" size={16} />저장됨</> : '저장'}
            </Button>
            <Button onClick={saveAndLeave} disabled={saving} className="resume-edit__leave">{dirty ? '저장하고 나가기' : '나가기'}</Button>
          </div>
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
            <PhotoField value={draft.photoUrl} onChange={(v) => change('photoUrl', v)} disabled={saving} />
            <BasicInfoFields basic={basic} onChange={(v) => changeContent('basic', v)} />
          </section>

          <section id="skills" className="resume-block">
            <div className="resume-block__head"><h2>기술 스택</h2></div>
            <SkillsEditor skills={content.skills || []} onChange={(v) => changeContent('skills', v)} />
          </section>

          <section id="projects" className="resume-block">
            <div className="resume-block__head"><h2>프로젝트</h2></div>
            <ProjectsEditor projects={content.projects || []} onChange={(v) => changeContent('projects', v)} />
          </section>

          <section id="awards" className="resume-block">
            <div className="resume-block__head"><h2>수상</h2></div>
            <AwardsEditor awards={content.awards || []} onChange={(v) => changeContent('awards', v)} />
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
