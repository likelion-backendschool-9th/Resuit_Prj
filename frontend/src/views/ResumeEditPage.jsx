'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { COMMON_RESUME, JOBS } from '../data/mock.js';
import { getCommonResumes, getTargetResumes, getTargetResume, createTargetResume, updateTargetResume, deleteTargetResume } from '../services/resumes.js';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import './ResumeEditPage.css';

// 공고 API가 아직 없어서, 서버에 없는 공고로 들어오면 목업 공고로 예시 화면을 보여 준다
const DEMO_CODES = new Set(['JOB_NOT_FOUND', 'NOT_SPECIAL_JOB']);

// 왼쪽 목차: 누른 섹션을 표시하고 그 위치로 스크롤한다.
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

const projectKey = (p, i) => p.id || `p${i}`;

// 서버 응답과 목업을 같은 모양으로 맞춘다
function fromApi(detail) {
  return {
    id: detail.id,
    header: `${detail.job.companyName} · ${detail.job.title}`,
    back: { href: '/resumes', label: '내 이력서로 돌아가기' },
    blank: !detail.commonResumeId,
    content: detail.content || {},
    questions: detail.questions.map((q) => ({ key: q.id, text: q.content, limit: q.charLimit || undefined })),
    answers: Object.fromEntries((detail.content?.answers || []).map((a) => [a.questionId, a.answer || '']))
  };
}

function fromMock(jobId, mode) {
  const job = JOBS.find((j) => String(j.id) === jobId && j.type === 'special') || JOBS[0];
  return {
    id: null,
    header: `${job.company} · ${job.role}`,
    back: { href: `/jobs/${job.id}`, label: '공고로 돌아가기' },
    blank: mode === 'blank',
    content: mode === 'blank' ? {} : COMMON_RESUME,
    questions: job.questions.map((q, i) => ({ key: i, text: q.text, limit: q.limit, placeholder: q.placeholder, hint: q.hint })),
    answers: {}
  };
}

// 특화 이력서 작성.
// id: 특화 이력서 ID (이어서 쓰기) / jobId + mode: 공고에서 새로 시작 (base: 공통 이력서 불러오기 | blank: 처음부터)
export default function ResumeEditPage({ id, jobId, mode }) {
  const router = useRouter();
  const [doc, setDoc] = useState(null);
  const [demoNotice, setDemoNotice] = useState('');
  const [error, setError] = useState('');
  const [answers, setAnswers] = useState({});
  const [picked, setPicked] = useState({});
  const [savedLabel, setSavedLabel] = useState('');
  const [saving, setSaving] = useState(false);
  const [activeToc, setActiveToc] = useState('q1');

  useEffect(() => {
    const controller = new AbortController();
    const { signal } = controller;
    const show = (next) => {
      setDoc(next);
      setAnswers(next.answers);
      setPicked(Object.fromEntries((next.content.projects || []).map((p, i) => [projectKey(p, i), p.selected !== false])));
    };
    const demo = (message) => { setDemoNotice(message); show(fromMock(jobId, mode)); };

    async function load() {
      if (id) {
        show(fromApi(await getTargetResume(id, signal)));
        setSavedLabel('저장된 내용을 불러왔어요');
        return;
      }
      if (!jobId || !/^\d+$/.test(jobId)) return demo('예시 공고라서 작성한 내용이 저장되지 않아요.');
      // 이 공고의 특화 이력서가 이미 있으면 그걸 이어서 쓴다
      const existing = (await getTargetResumes(signal)).find((r) => String(r.jobId) === jobId);
      if (existing) return router.replace(`/resumes/edit?id=${existing.id}`);
      const base = mode === 'blank' ? null : (await getCommonResumes(signal))[0];
      const created = await createTargetResume({ jobId: Number(jobId), mode: base ? 'BASE' : 'BLANK', commonResumeId: base?.id });
      router.replace(`/resumes/edit?id=${created.id}`);
    }

    load().catch((err) => {
      if (signal.aborted) return;
      if (DEMO_CODES.has(err.code)) demo('아직 서버에 없는 예시 공고라서 작성한 내용이 저장되지 않아요.');
      else if (err.status === 401 && !id) demo('로그인하면 작성한 내용이 저장돼요. 지금은 예시 화면이에요.');
      else setError(err.message);
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
  const pickedCount = projects.filter((p, i) => picked[projectKey(p, i)]).length;
  const toc = { active: activeToc, onPick: setActiveToc };

  async function save(after) {
    if (!doc.id) { setSavedLabel('예시 화면이라 저장되지 않아요'); return; }
    setSaving(true);
    try {
      const next = {
        ...content,
        projects: projects.map((p, i) => ({ ...p, selected: !!picked[projectKey(p, i)] })),
        answers: questions.map((q) => ({ questionId: q.key, answer: answers[q.key] || '' }))
      };
      await updateTargetResume(doc.id, { content: next });
      setSavedLabel('임시 저장됨 · 방금 전');
      after?.();
    } catch (err) {
      setSavedLabel(err.message);
    } finally {
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
            <Button onClick={() => save(() => router.push('/resumes'))} disabled={saving}>제출하기</Button>
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
          {questions.map((q, i) => <TocLink key={q.key} target={`q${i + 1}`} {...toc}>문항 {i + 1}</TocLink>)}
        </nav>

        <main className="resume-edit__form">
          {demoNotice && <div className="resume-edit__note">{demoNotice}</div>}
          {blank && <div className="resume-edit__note">빈 이력서로 시작했어요. 아래 항목을 이 회사에 맞춰 새로 작성해 주세요.</div>}

          <section id="basic" className="resume-block">
            <div className="resume-block__head">
              <h2>기본 정보</h2>
              {!blank && <Badge tone="loaded">공통에서 불러옴</Badge>}
              {!blank && <button type="button" className="resume-block__edit">이 이력서에서만 수정</button>}
            </div>
            {!content.basic ? <span className="small muted">아직 작성하지 않았어요</span> : (
              <div className="grid-3" style={{ fontSize: 14 }}>
                <div><div className="xs muted">이름</div><div style={{ fontWeight: 500 }}>{content.basic.name}</div></div>
                <div><div className="xs muted">학력</div><div style={{ fontWeight: 500 }}>{content.basic.school}</div></div>
                <div><div className="xs muted">GitHub</div><div style={{ fontWeight: 500 }}>{content.basic.github}</div></div>
              </div>
            )}
          </section>

          <section id="skills" className="resume-block">
            <div className="resume-block__head"><h2>기술 스택</h2>{!blank && <Badge tone="loaded">공통에서 불러옴</Badge>}</div>
            {!content.skills?.length ? <span className="small muted">아직 작성하지 않았어요</span> : (
              <div className="row-wrap">{content.skills.map((s) => <span key={s} className="resume-skill">{s}</span>)}</div>
            )}
          </section>

          <section id="projects" className="resume-block">
            <div className="resume-block__head"><h2>프로젝트</h2>{!blank && projects.length > 0 && <Badge tone="loaded">공통에서 불러옴 · {projects.length}개 중 {pickedCount}개 선택</Badge>}</div>
            {!projects.length ? <span className="small muted">아직 작성하지 않았어요</span> : projects.map((p, i) => {
              const key = projectKey(p, i);
              return (
                <div key={key} className="resume-project">
                  <label className="resume-project__check">
                    <input type="checkbox" checked={!!picked[key]} onChange={(e) => setPicked((m) => ({ ...m, [key]: e.target.checked }))} />
                    <span><span className="bold" style={{ display: 'block' }}>{p.title}</span><span className="small muted">{p.desc}</span></span>
                  </label>
                  {p.evidence && (
                    <div className="resume-project__evidence">
                      <span className="xs bold muted">근거</span>
                      {p.evidence.map((ev) => {
                        const type = String(ev.type).toLowerCase();
                        return (
                          <Link key={ev.label} href="/profile" className={`evidence-chip evidence-chip--${type}`}>
                            <Icon name={type === 'repo' ? 'repo' : 'rss'} size={14} />{ev.label}
                          </Link>
                        );
                      })}
                      {p.canAttach && <button type="button" className="evidence-chip evidence-chip--add">레포 · 블로그 글 첨부</button>}
                    </div>
                  )}
                </div>
              );
            })}
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
