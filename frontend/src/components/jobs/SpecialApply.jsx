'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { applyWithTargetResume } from '@/services/jobApplications.js';
import { createTargetResume, getTargetResume } from '@/services/resumes.js';
import Button from '@/components/common/Button.jsx';
import { FIELD_LABEL } from '@/components/feedback/labels.js';

// 특화 공고 지원: 이 공고용 특화 이력서가 없으면 작성을 시작하고, 있으면 이어서 쓰거나 그 이력서로 지원한다.
export default function SpecialApply({ job, resumes, onApplied }) {
  const target = resumes.targetResumes.find((r) => r.jobId === job.id);
  return target
    ? <ApplyWithTarget job={job} target={target} onApplied={onApplied} />
    : <StartTarget job={job} commonResumes={resumes.commonResumes} />;
}

// 작성 시작: 공통 이력서 하나를 골라 내용을 불러오거나 빈 이력서로 시작한다
function StartTarget({ job, commonResumes }) {
  const router = useRouter();
  const hasCommon = commonResumes.length > 0;
  const [mode, setMode] = useState(hasCommon ? 'BASE' : 'BLANK');
  const [pickedId, setPickedId] = useState(commonResumes[0]?.id);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const modes = [
    { id: 'BASE', title: '공통 이력서로 시작', desc: hasCommon ? '고른 공통 이력서의 기본 정보 · 기술 · 프로젝트를 불러오고 요구 문항만 새로 작성' : '공통 이력서가 없어서 고를 수 없어요', disabled: !hasCommon },
    { id: 'BLANK', title: '처음부터 작성', desc: '빈 이력서에서 이 회사에 맞춰 전부 새로 작성' }
  ];

  async function start() {
    setBusy(true);
    setError('');
    try {
      const created = await createTargetResume({ jobId: job.id, mode, commonResumeId: mode === 'BASE' ? pickedId : undefined });
      router.push(`/resumes/edit?id=${created.id}`);
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  return (
    <div className="stack" style={{ gap: 14 }}>
      <p className="muted" style={{ fontSize: 14 }}>특화 공고는 위의 요구 문항에 답한 특화 이력서로 지원해요. 먼저 이 공고용 특화 이력서를 만들어 주세요.</p>
      <div className="grid-2" role="radiogroup" aria-label="작성 방식">
        {modes.map((m) => (
          <button key={m.id} type="button" role="radio" aria-checked={mode === m.id} disabled={m.disabled} className={`choice${mode === m.id ? ' is-on' : ''}`} onClick={() => setMode(m.id)}>
            <span className="choice__title">{m.title}</span>
            <span className="choice__desc">{m.desc}</span>
          </button>
        ))}
      </div>

      {mode === 'BASE' && (
        <div className="stack" role="radiogroup" aria-label="불러올 공통 이력서">
          {commonResumes.map((r) => {
            const on = r.id === pickedId;
            return (
              <div key={r.id} className={`resume-pick${on ? ' is-on' : ''}`}>
                <button type="button" role="radio" aria-checked={on} className="resume-pick__main" onClick={() => setPickedId(r.id)}>
                  <span className="resume-pick__radio" aria-hidden="true"><span /></span>
                  <span className="resume-pick__info">
                    <span className="row-wrap" style={{ gap: 6 }}>
                      <strong>{r.title}</strong>
                      {r.field && <span className="badge badge--general badge--sm">{FIELD_LABEL[r.field] || r.field}</span>}
                    </span>
                  </span>
                </button>
              </div>
            );
          })}
        </div>
      )}

      <div className="row-wrap" style={{ gap: 10 }}>
        <Button size="lg" className="job-apply__submit" onClick={start} disabled={busy || (mode === 'BASE' && !pickedId)}>
          {busy ? '만드는 중…' : mode === 'BASE' ? '공통 이력서 불러와서 작성하기' : '빈 이력서로 작성하기'}
        </Button>
      </div>
      {error && <p className="small" role="alert">{error}</p>}
    </div>
  );
}

// 작성 중인 특화 이력서: 문항 작성 현황을 보여 주고 이어서 쓰거나 바로 지원한다
function ApplyWithTarget({ job, target, onApplied }) {
  const [progress, setProgress] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    getTargetResume(target.id, controller.signal)
      .then((detail) => {
        const answered = new Set((detail.content?.answers || []).filter((a) => String(a.answer ?? '').trim()).map((a) => a.questionId));
        setProgress({ total: detail.questions.length, done: detail.questions.filter((q) => answered.has(q.id)).length });
      })
      .catch(() => { if (!controller.signal.aborted) setProgress(null); });
    return () => controller.abort();
  }, [target.id]);

  async function apply() {
    setBusy(true);
    setError('');
    try {
      await applyWithTargetResume(job.id, target.id);
      onApplied();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  const left = progress ? progress.total - progress.done : 0;

  return (
    <div className="stack" style={{ gap: 14 }}>
      <div className="resume-pick is-on">
        <div className="resume-pick__main">
          <span className="resume-thumb" aria-hidden="true"><span /><span /><span /><span /></span>
          <span className="resume-pick__info">
            <strong>이 공고용 특화 이력서</strong>
            <span className="small muted">{target.commonResumeId ? '공통 이력서 기반' : '처음부터 작성'} · 요구 문항 {target.questionCount}개</span>
            {progress && <span className="small" style={{ color: 'var(--accent-text)', fontWeight: 500 }}>요구 문항 {progress.total}개 중 {progress.done}개 작성</span>}
          </span>
        </div>
      </div>
      {left > 0 && <p className="small" style={{ color: 'var(--special)' }}>아직 답하지 않은 요구 문항이 {left}개 있어요. 먼저 작성하는 것을 권해요.</p>}
      <div className="row-wrap" style={{ gap: 10 }}>
        <Button size="lg" variant="outline" to={`/resumes/edit?id=${target.id}`}>이어서 쓰기</Button>
        <Button size="lg" className="job-apply__submit" onClick={apply} disabled={busy}>{busy ? '지원하는 중…' : '이 특화 이력서로 지원하기'}</Button>
      </div>
      {error && <p className="small" role="alert">{error}</p>}
    </div>
  );
}
