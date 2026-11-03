import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAppState } from '../../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../layout/AppLayout.jsx';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import JobHead from './JobHead.jsx';

const STEPS = ['공고 확인', '공통 이력서 선택', '지원 완료'];

// 일반 공고 상세: 공통 이력서를 골라 그대로 제출한다.
export default function GeneralJobDetail({ job }) {
  const { commonResumes, applied, applyJob, cancelApply } = useAppState();
  const recommended = commonResumes.find((r) => r.field === job.field) || commonResumes[0];
  const [pickedId, setPickedId] = useState(recommended?.id);
  const [previewOpen, setPreviewOpen] = useState({});
  const [withGithub, setWithGithub] = useState(true);
  const submission = applied[job.id];
  const picked = commonResumes.find((r) => r.id === pickedId);

  return (
    <>
      <MainColumn>
        <div className="page-head">
          <Link to="/jobs" className="icon-btn icon-btn--plain" aria-label="공고 목록으로"><Icon name="back" /></Link>
          <h1>공고</h1>
        </div>

        <JobHead job={job} />

        {!submission && (
          <section className="section" style={{ padding: 24 }}>
            <div className="row-wrap" style={{ justifyContent: 'space-between', alignItems: 'baseline', gap: 12 }}>
              <h3 className="section-title">제출할 공통 이력서 선택</h3>
              <Link to="/resumes" className="text-link">새 공통 이력서 만들기</Link>
            </div>
            <p className="muted" style={{ fontSize: 14 }}>일반 공고는 추가 문항 없이 공통 이력서를 그대로 제출해요. 지원 직무와 분야가 맞는 이력서를 추천해 드려요.</p>

            <div className="stack" role="radiogroup" aria-label="제출할 공통 이력서">
              {commonResumes.map((r) => {
                const on = r.id === pickedId;
                const open = !!previewOpen[r.id];
                return (
                  <div key={r.id} className={`resume-pick${on ? ' is-on' : ''}`}>
                    <button type="button" role="radio" aria-checked={on} className="resume-pick__main" onClick={() => setPickedId(r.id)}>
                      <span className="resume-pick__radio" aria-hidden="true"><span /></span>
                      <span className="resume-thumb" aria-hidden="true"><span /><span /><span /><span /></span>
                      <span className="resume-pick__info">
                        <span className="row-wrap" style={{ gap: 6 }}>
                          <strong>{r.name}</strong>
                          <span className="badge badge--general badge--sm">{r.field}</span>
                          {r.field === job.field && <span className="badge badge--accepted badge--sm">이 공고에 추천</span>}
                        </span>
                        <span className="small muted">{r.sections} · {r.updated}</span>
                        <span className="small" style={{ color: 'var(--accent-text)', fontWeight: 500 }}>받은 피드백 {r.feedback}개</span>
                      </span>
                    </button>
                    <div className="resume-pick__foot">
                      <Button variant="outline" size="sm" aria-expanded={open} onClick={() => setPreviewOpen((m) => ({ ...m, [r.id]: !open }))}>
                        {open ? '미리보기 접기' : '미리보기'}
                      </Button>
                    </div>
                    {open && (
                      <div className="resume-pick__preview">
                        <div className="row-wrap" style={{ gap: 6 }}>
                          <span className="xs bold muted" style={{ marginRight: 4 }}>기술 스택</span>
                          {r.skills.map((s) => <span key={s} className="skill-chip">{s}</span>)}
                        </div>
                        <div className="stack" style={{ gap: 6 }}>
                          <span className="xs bold muted">프로젝트</span>
                          {r.projects.map((p) => <div key={p.title}><strong>{p.title}</strong> <span className="muted">· {p.desc}</span></div>)}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>

            <label className="check" style={{ gap: 10, fontSize: 14 }}>
              <input type="checkbox" checked={withGithub} onChange={(e) => setWithGithub(e.target.checked)} />
              GitHub 잔디 · 대표 레포를 함께 보내기
            </label>

            <div className="row-wrap" style={{ gap: 10 }}>
              <Button
                size="lg"
                className="job-apply__submit"
                disabled={!picked}
                onClick={() => applyJob(job.id, { resumeTitle: picked.name, withGithub })}
              >
                {picked ? `「${picked.name}」로 지원하기` : '공통 이력서를 만들어 주세요'}
              </Button>
              <Button variant="accent-outline" size="lg" to="/feedback" className="job-apply__feedback">제출 전에 피드백 받기</Button>
            </div>
          </section>
        )}

        {submission && (
          <section className="section" role="status" style={{ padding: 24, gap: 16 }}>
            <div className="job-applied">
              <span className="job-applied__icon" aria-hidden="true"><Icon name="check" size={24} /></span>
              <div className="stack" style={{ gap: 2 }}>
                <strong style={{ fontSize: 18 }}>{job.company}에 지원했어요</strong>
                <span className="text-2" style={{ fontSize: 14 }}>결과가 나오면 알림으로 알려 드려요.</span>
              </div>
            </div>
            <div className="job-applied__rows">
              <span className="muted">공고</span><span>{job.title}</span>
              <span className="muted">제출한 이력서</span><span className="bold">{submission.resumeTitle}</span>
              <span className="muted">함께 보낸 정보</span><span>{submission.withGithub ? 'GitHub 잔디 · 대표 레포' : '없음'}</span>
              <span className="muted">지원 일시</span><span>방금 전</span>
            </div>
            <div className="row-wrap" style={{ gap: 10 }}>
              <Button to="/resumes" className="job-applied__action">지원 현황 보기</Button>
              <Button variant="outline" to="/jobs" className="job-applied__action">다른 공고 보기</Button>
              <button type="button" className="job-applied__cancel" onClick={() => cancelApply(job.id)}>지원 취소</button>
            </div>
          </section>
        )}
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>지원 순서</h2>
          {STEPS.map((label, i) => {
            const no = i + 1;
            const state = submission || no === 1 ? 'done' : no === 2 ? 'now' : 'todo';
            return (
              <div key={label} className={`apply-step apply-step--${state}`} aria-current={state === 'now' ? 'step' : undefined}>
                <span className="apply-step__no">{no}</span><span>{label}</span>
              </div>
            );
          })}
        </section>
        <section className="card">
          <h2>지원 방식</h2>
          <div className="stack" style={{ gap: 4 }}><span className="xs bold text-2">일반 공고</span><span className="text-2" style={{ fontSize: 14 }}>공통 이력서를 골라 그대로 제출</span></div>
          <div className="stack" style={{ gap: 4 }}><span className="xs bold" style={{ color: 'var(--special)' }}>특화 공고</span><span className="text-2" style={{ fontSize: 14 }}>회사 요구 문항을 담은 특화 이력서를 공고에서 바로 작성해 제출</span></div>
        </section>
      </RightAside>
    </>
  );
}
