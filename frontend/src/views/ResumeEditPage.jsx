'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getCommonResumes, getTargetResumes, getTargetResume, createTargetResume, updateTargetResume, deleteTargetResume } from '../services/resumes.js';
import { applyWithTargetResume } from '../services/jobApplications.js';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import TocLink from '../components/resume/TocLink.jsx';
import BasicInfoFields from '../components/resume/BasicInfoFields.jsx';
import PhotoField from '../components/resume/PhotoField.jsx';
import SkillsEditor from '../components/resume/SkillsEditor.jsx';
import { TextAreaField } from '../components/resume/ResumeFields.jsx';
import ProjectsEditor from '../components/resume/ProjectsEditor.jsx';
import AwardsEditor from '../components/resume/AwardsEditor.jsx';
import { normalizeContent, cleanContent } from '../components/resume/resumeContent.js';
import './ResumeEditPage.css';

const MAX_INTRODUCTION = 2000;

// 프로젝트의 selected가 없으면 이 회사에 제출하는 것으로 본다
const withSelected = (content) => ({
  ...content,
  ...(Array.isArray(content.projects) && { projects: content.projects.map((p) => ({ ...p, selected: p.selected !== false })) })
});

// 서버 응답을 화면에서 사용하는 모양으로 맞춘다
function fromApi(detail) {
  return {
    id: detail.id,
    jobId: detail.job.id,
    // SUBMITTED: 이 특화 이력서로 이미 지원했다
    submitted: detail.status === 'SUBMITTED',
    header: `${detail.job.companyName} · ${detail.job.title}`,
    back: { href: '/resumes', label: '내 이력서로 돌아가기' },
    blank: !detail.commonResumeId,
    content: withSelected(normalizeContent(detail.content || {})),
    // 특화 이력서 사진. 공통 이력서에서 시작하면 그 사진이 복사돼 있다.
    photoUrl: detail.photoUrl || '',
    questions: detail.questions.map((q) => ({ key: q.id, text: q.content, limit: q.charLimit || undefined })),
    answers: Object.fromEntries((detail.content?.answers || []).map((a) => [a.questionId, a.answer || '']))
  };
}

// 특화 이력서 작성.
// id: 특화 이력서 ID (이어서 쓰기) / jobId + mode: 공고에서 새로 시작 (base: 공통 이력서 불러오기 | blank: 처음부터)
export default function ResumeEditPage({ id, jobId, mode }) {
  const router = useRouter();
  const [doc, setDoc] = useState(null);
  const [error, setError] = useState('');
  const [answers, setAnswers] = useState({});
  const [savedLabel, setSavedLabel] = useState('');
  const [saving, setSaving] = useState(false);
  const [activeToc, setActiveToc] = useState('q1');

  useEffect(() => {
    const controller = new AbortController();
    const { signal } = controller;
    const show = (next) => {
      setDoc(next);
      setAnswers(next.answers);
    };

    async function load() {
      if (id) {
        show(fromApi(await getTargetResume(id, signal)));
        setSavedLabel('저장된 내용을 불러왔어요');
        return;
      }
      if (!jobId || !/^\d+$/.test(jobId)) {
        setError('작성할 공고를 찾을 수 없어요.');
        return;
      }
      // 이 공고의 특화 이력서가 이미 있으면 그걸 이어서 쓴다
      const existing = (await getTargetResumes(signal)).find((r) => String(r.jobId) === jobId);
      if (existing) return router.replace(`/resumes/edit?id=${existing.id}`);
      const base = mode === 'blank' ? null : (await getCommonResumes(signal))[0];
      const created = await createTargetResume({ jobId: Number(jobId), mode: base ? 'BASE' : 'BLANK', commonResumeId: base?.id });
      router.replace(`/resumes/edit?id=${created.id}`);
    }

    load().catch((err) => {
      if (signal.aborted) return;
      setError(err.message);
    });
    return () => controller.abort();
  }, [id, jobId, mode, router]);

  if (error || !doc) {
    return (
      <div className="resume-edit">
        <div className="empty" role={error ? 'alert' : 'status'} style={{ margin: 40 }}>
          {error ? <><p>{error}</p><Button variant="outline" to="/resumes">내 이력서로</Button></> : '특화 이력서를 불러오는 중이에요.'}
        </div>
      </div>
    );
  }

  const { content, questions, blank } = doc;
  const projects = content.projects || [];
  const done = questions.filter((q) => (answers[q.key] || '').trim()).length;
  const total = questions.length;
  const pickedCount = projects.filter((p) => p.selected !== false).length;
  const toc = { active: activeToc, onPick: setActiveToc };
  // 특화 이력서는 시작할 때 복사한 별도 내용이라 여기서 고쳐도 공통 이력서는 바뀌지 않는다
  const changeContent = (key, value) => setDoc((d) => ({ ...d, content: { ...d.content, [key]: value } }));

  // 저장에 성공하면 true
  async function save() {
    if (!doc.id) { setSavedLabel('예시 화면이라 저장되지 않아요'); return false; }
    setSaving(true);
    try {
      // 공백을 지우고 빈 프로젝트·수상은 뺀다. 프로젝트의 제출 여부(selected)는 그대로 남는다
      const next = {
        ...cleanContent(content),
        answers: questions.map((q) => ({ questionId: q.key, answer: answers[q.key] || '' }))
      };
      const saved = await updateTargetResume(doc.id, { content: next, photoUrl: doc.photoUrl || '' });
      // 방금 올린 사진(tmpImage)은 저장되면 정식 주소로 바뀐다. 다음 저장에 다시 보내지 않도록 바꿔 둔다.
      setDoc((d) => ({ ...d, photoUrl: saved.photoUrl || '' }));
      setSavedLabel('임시 저장됨 · 방금 전');
      return true;
    } catch (err) {
      setSavedLabel(err.message);
      return false;
    } finally {
      setSaving(false);
    }
  }

  // 저장한 뒤 이 특화 이력서로 공고에 지원하고 공고 상세로 간다. 이미 지원했으면 저장만 하고 간다
  async function submit() {
    if (!doc.submitted) {
      const left = total - done;
      const ask = left > 0 ? `아직 답하지 않은 요구 문항이 ${left}개 있어요. 그래도 이 특화 이력서로 지원할까요?` : '이 특화 이력서로 지원할까요?';
      if (!window.confirm(ask)) return;
    }
    if (!(await save())) return;
    if (doc.submitted) { router.push(`/jobs/${doc.jobId}`); return; }
    setSaving(true);
    try {
      await applyWithTargetResume(doc.jobId, doc.id);
      router.push(`/jobs/${doc.jobId}`);
    } catch (err) {
      setSavedLabel(err.message);
      setSaving(false);
    }
  }

  async function remove() {
    if (!window.confirm('이 특화 이력서를 삭제할까요?')) return;
    setSaving(true);
    try {
      await deleteTargetResume(doc.id);
      router.push('/resumes');
    } catch (err) {
      setSavedLabel(err.message);
      setSaving(false);
    }
  }

  return (
    <div className="resume-edit">
      <header className="resume-edit__bar">
        <div className="resume-edit__bar-in">
          <Link href={doc.back.href} className="icon-btn icon-btn--plain" aria-label={doc.back.label}><Icon name="back" /></Link>
          <div className="grow" style={{ flexBasis: 320 }}>
            <div className="row" style={{ gap: 6 }}><Badge tone="special" size="md">특화 이력서</Badge><span className="small muted" role="status">{savedLabel}</span></div>
            <h1 className="resume-edit__title">{doc.header}</h1>
          </div>
          <div className="row">
            <ThemeToggle />
            {doc.id && <Button variant="outline" onClick={remove} disabled={saving}>삭제</Button>}
            <Button variant="outline" onClick={() => save()} disabled={saving}>임시 저장</Button>
            <Button onClick={submit} disabled={saving}>{doc.submitted ? '저장하고 공고 보기' : '저장하고 지원하기'}</Button>
          </div>
        </div>
      </header>

      <div className="resume-edit__layout">
        <nav className="resume-edit__toc" aria-label="이력서 섹션">
          <div className="resume-edit__toc-group">{blank ? '기본 항목' : '공통 이력서에서 불러옴'}</div>
          <TocLink target="basic" {...toc}>기본 정보</TocLink>
          <TocLink target="skills" {...toc}>기술 스택</TocLink>
          <TocLink target="projects" {...toc}>프로젝트</TocLink>
          <TocLink target="awards" {...toc}>수상</TocLink>
          <TocLink target="introduction" {...toc}>자기소개</TocLink>
          <div className="resume-edit__toc-group is-req">회사 요구 문항</div>
          {questions.map((q, i) => <TocLink key={q.key} target={`q${i + 1}`} {...toc}>문항 {i + 1}</TocLink>)}
        </nav>

        <main className="resume-edit__form">
          <div className="resume-edit__note">
            {blank ? '빈 이력서로 시작했어요. 아래 항목을 이 회사에 맞춰 새로 작성해 주세요.'
              : '공통 이력서에서 불러온 내용이에요. 이 회사에 맞춰 고쳐도 원래 공통 이력서는 바뀌지 않아요.'}
          </div>

          {content.submittedQuestions?.length > 0 && (
            <details className="resume-block">
              <summary>불러온 제출 당시 문항과 답변</summary>
              <p className="small muted">이전 제출 자료입니다. 재지원할 때는 아래의 현재 회사 요구 문항을 확인해 주세요.</p>
              {content.submittedQuestions.map((question) => (
                <div key={question.id}>
                  <h3>{question.content}</h3>
                  <p className="resume-edit__historic-answer">{content.submittedAnswers?.find((answer) => answer.questionId === question.id)?.answer || '답변 없음'}</p>
                </div>
              ))}
            </details>
          )}

          <section id="basic" className="resume-block">
            <div className="resume-block__head"><h2>기본 정보</h2>{!blank && <Badge tone="loaded">공통에서 불러옴</Badge>}</div>
            <PhotoField value={doc.photoUrl} onChange={(v) => setDoc((d) => ({ ...d, photoUrl: v }))} disabled={saving} />
            <BasicInfoFields basic={content.basic} onChange={(v) => changeContent('basic', v)} />
          </section>

          <section id="skills" className="resume-block">
            <div className="resume-block__head"><h2>기술 스택</h2>{!blank && <Badge tone="loaded">공통에서 불러옴</Badge>}</div>
            <SkillsEditor skills={content.skills || []} onChange={(v) => changeContent('skills', v)} />
          </section>

          <section id="projects" className="resume-block">
            <div className="resume-block__head">
              <h2>프로젝트</h2>
              {projects.length > 0 && <Badge tone="loaded">{blank ? '' : '공통에서 불러옴 · '}{projects.length}개 중 {pickedCount}개 제출</Badge>}
            </div>
            <ProjectsEditor selectable projects={projects} onChange={(v) => changeContent('projects', v)} />
          </section>

          <section id="awards" className="resume-block">
            <div className="resume-block__head"><h2>수상</h2>{!blank && content.awards?.length > 0 && <Badge tone="loaded">공통에서 불러옴</Badge>}</div>
            <AwardsEditor awards={content.awards || []} onChange={(v) => changeContent('awards', v)} />
          </section>

          <section id="introduction" className="resume-block">
            <div className="resume-block__head"><h2>자기소개</h2>{!blank && content.introduction && <Badge tone="loaded">공통에서 불러옴</Badge>}</div>
            <TextAreaField
              id="introduction-input" label="자기소개" max={MAX_INTRODUCTION} rows={6}
              placeholder="이 회사에 맞춰 어떤 개발자인지 소개해 주세요."
              value={content.introduction} onChange={(v) => changeContent('introduction', v)}
            />
          </section>

          {questions.map((q, i) => (
            <section key={q.key} id={`q${i + 1}`} className="resume-block resume-block--req">
              <span style={{ alignSelf: 'flex-start' }}><Badge tone="special" size="md">회사 요구 문항 {i + 1} · 필수</Badge></span>
              <label htmlFor={`q${i + 1}-input`} className="bold" style={{ fontSize: 16 }}>{q.text}</label>
              <textarea
                id={`q${i + 1}-input`} className="textarea" rows={6} maxLength={q.limit}
                placeholder={q.placeholder}
                value={answers[q.key] || ''} onChange={(e) => setAnswers((a) => ({ ...a, [q.key]: e.target.value }))}
              />
              <div className="resume-block__foot small muted">
                <span>{q.hint}</span>
                <span>{(answers[q.key] || '').length}{q.limit ? ` / ${q.limit}` : ''}자</span>
              </div>
            </section>
          ))}
        </main>

        <aside className="resume-edit__side">
          <section className="card">
            <h2 style={{ fontSize: 16 }}>작성 현황</h2>
            <div className="progress"><div style={{ width: `${total ? (done / total) * 100 : 0}%` }} /></div>
            <div className="text-2" style={{ fontSize: 14 }}>필수 문항 {done} / {total} 작성</div>
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
