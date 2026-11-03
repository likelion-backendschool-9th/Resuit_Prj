import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { COMMON_RESUME, JOBS } from '../data/mock.js';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import './ResumeEditPage.css';

// HashRouter 에서는 href="#id" 가 라우트로 해석되므로 버튼으로 스크롤한다.
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

// 특화 이력서 작성. ?job=공고ID&mode=base(공통 이력서 불러오기)|blank(처음부터)
export default function ResumeEditPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const job = JOBS.find((j) => String(j.id) === params.get('job') && j.type === 'special') || JOBS[0];
  const blank = params.get('mode') === 'blank';
  const [answers, setAnswers] = useState(() => job.questions.map(() => ''));
  const [savedLabel, setSavedLabel] = useState('자동 저장됨 · 방금 전');
  const [picked, setPicked] = useState({ p1: true, p2: true, p3: false });
  const [activeToc, setActiveToc] = useState('q1');

  const done = answers.filter((a) => a.trim()).length;
  const total = job.questions.length;
  const projects = COMMON_RESUME.projects;
  const pickedCount = projects.filter((p) => picked[p.id]).length;
  const toc = { active: activeToc, onPick: setActiveToc };

  return (
    <div className="resume-edit">
      <header className="resume-edit__bar">
        <div className="resume-edit__bar-in">
          <Link to={`/jobs/${job.id}`} className="icon-btn icon-btn--plain" aria-label="공고로 돌아가기"><Icon name="back" /></Link>
          <div className="grow" style={{ flexBasis: 320 }}>
            <div className="row" style={{ gap: 6 }}><Badge tone="special" size="md">특화 이력서</Badge><span className="small muted">{savedLabel}</span></div>
            <h1 className="resume-edit__title">{job.company} · {job.role}</h1>
          </div>
          <div className="row">
            <ThemeToggle />
            <Button variant="outline" onClick={() => setSavedLabel('임시 저장됨 · 방금 전')}>임시 저장</Button>
            <Button onClick={() => navigate('/resumes')}>제출하기</Button>
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
          {job.questions.map((_, i) => <TocLink key={i} target={`q${i + 1}`} {...toc}>문항 {i + 1}</TocLink>)}
        </nav>

        <main className="resume-edit__form">
          {blank && <div className="resume-edit__note">빈 이력서로 시작했어요. 아래 항목을 이 회사에 맞춰 새로 작성해 주세요.</div>}

          <section id="basic" className="resume-block">
            <div className="resume-block__head">
              <h2>기본 정보</h2>
              {!blank && <Badge tone="loaded">공통에서 불러옴</Badge>}
              {!blank && <button type="button" className="resume-block__edit">이 이력서에서만 수정</button>}
            </div>
            {blank ? <span className="small muted">아직 작성하지 않았어요</span> : (
              <div className="grid-3" style={{ fontSize: 14 }}>
                <div><div className="xs muted">이름</div><div style={{ fontWeight: 500 }}>{COMMON_RESUME.basic.name}</div></div>
                <div><div className="xs muted">학력</div><div style={{ fontWeight: 500 }}>{COMMON_RESUME.basic.school}</div></div>
                <div><div className="xs muted">GitHub</div><div style={{ fontWeight: 500 }}>{COMMON_RESUME.basic.github}</div></div>
              </div>
            )}
          </section>

          <section id="skills" className="resume-block">
            <div className="resume-block__head"><h2>기술 스택</h2>{!blank && <Badge tone="loaded">공통에서 불러옴</Badge>}</div>
            {blank ? <span className="small muted">아직 작성하지 않았어요</span> : (
              <div className="row-wrap">{COMMON_RESUME.skills.map((s) => <span key={s} className="resume-skill">{s}</span>)}</div>
            )}
          </section>

          <section id="projects" className="resume-block">
            <div className="resume-block__head"><h2>프로젝트</h2>{!blank && <Badge tone="loaded">공통에서 불러옴 · {projects.length}개 중 {pickedCount}개 선택</Badge>}</div>
            {blank ? <span className="small muted">아직 작성하지 않았어요</span> : projects.map((p) => (
              <div key={p.id} className="resume-project">
                <label className="resume-project__check">
                  <input type="checkbox" checked={!!picked[p.id]} onChange={(e) => setPicked((m) => ({ ...m, [p.id]: e.target.checked }))} />
                  <span><span className="bold" style={{ display: 'block' }}>{p.title}</span><span className="small muted">{p.desc}</span></span>
                </label>
                {p.evidence && (
                  <div className="resume-project__evidence">
                    <span className="xs bold muted">근거</span>
                    {p.evidence.map((ev) => (
                      <Link key={ev.label} to="/profile" className={`evidence-chip evidence-chip--${ev.type}`}>
                        <Icon name={ev.type === 'repo' ? 'repo' : 'rss'} size={14} />{ev.label}
                      </Link>
                    ))}
                    {p.canAttach && <button type="button" className="evidence-chip evidence-chip--add">레포 · 블로그 글 첨부</button>}
                  </div>
                )}
              </div>
            ))}
          </section>

          {job.questions.map((q, i) => (
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
