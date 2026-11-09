'use client';

import { useState } from 'react';
import Link from 'next/link';
import { applyWithCommonResume } from '@/services/jobApplications.js';
import Button from '@/components/common/Button.jsx';
import ResumePreview from '@/components/feedback/ResumePreview.jsx';
import { FIELD_LABEL, SECTION_LABEL } from '@/components/feedback/labels.js';

const COUNTED = new Set(['PROJECTS', 'AWARDS']);

// "기본 정보 · 기술 스택 · 프로젝트 3개"처럼 작성한 섹션을 줄여 보여 준다
function sectionText(sections) {
  if (!sections?.length) return '아직 작성한 섹션이 없어요';
  return sections.map((s) => (COUNTED.has(s.type) ? `${SECTION_LABEL[s.type]} ${s.count}개` : SECTION_LABEL[s.type])).join(' · ');
}

// 일반 공고 지원: 공통 이력서 하나를 골라 그대로 제출한다.
export default function GeneralApply({ job, commonResumes, onApplied }) {
  const [pickedId, setPickedId] = useState(commonResumes[0]?.id);
  const [previewId, setPreviewId] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const picked = commonResumes.find((r) => r.id === pickedId);

  if (commonResumes.length === 0) {
    return (
      <div className="stack" style={{ gap: 10 }}>
        <p className="muted">일반 공고는 공통 이력서로 지원해요. 아직 공통 이력서가 없어요.</p>
        <div><Button to="/resumes">공통 이력서 만들기</Button></div>
      </div>
    );
  }

  async function apply() {
    setBusy(true);
    setError('');
    try {
      await applyWithCommonResume(job.id, picked.id);
      onApplied();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  return (
    <div className="stack" style={{ gap: 14 }}>
      <div className="row-wrap" style={{ justifyContent: 'space-between', alignItems: 'baseline', gap: 12 }}>
        <p className="muted" style={{ fontSize: 14 }}>일반 공고는 추가 문항 없이 공통 이력서를 그대로 제출해요.</p>
        <Link href="/resumes" className="text-link">내 이력서 관리</Link>
      </div>

      <div className="stack" role="radiogroup" aria-label="제출할 공통 이력서">
        {commonResumes.map((r) => {
          const on = r.id === pickedId;
          const open = previewId === r.id;
          return (
            <div key={r.id} className={`resume-pick${on ? ' is-on' : ''}`}>
              <button type="button" role="radio" aria-checked={on} className="resume-pick__main" onClick={() => setPickedId(r.id)}>
                <span className="resume-pick__radio" aria-hidden="true"><span /></span>
                <span className="resume-thumb" aria-hidden="true"><span /><span /><span /><span /></span>
                <span className="resume-pick__info">
                  <span className="row-wrap" style={{ gap: 6 }}>
                    <strong>{r.title}</strong>
                    {r.field && <span className="badge badge--general badge--sm">{FIELD_LABEL[r.field] || r.field}</span>}
                  </span>
                  <span className="small muted">{sectionText(r.sections)}</span>
                  <span className="small" style={{ color: 'var(--accent-text)', fontWeight: 500 }}>받은 피드백 {r.feedbackCount}개</span>
                </span>
              </button>
              <div className="resume-pick__foot">
                <Button variant="outline" size="sm" aria-expanded={open} onClick={() => setPreviewId(open ? null : r.id)}>
                  {open ? '미리보기 접기' : '미리보기'}
                </Button>
              </div>
              {open && <div className="resume-pick__preview"><ResumePreview type="COMMON" id={r.id} /></div>}
            </div>
          );
        })}
      </div>

      <div className="row-wrap" style={{ gap: 10 }}>
        <Button size="lg" className="job-apply__submit" onClick={apply} disabled={!picked || busy}>
          {busy ? '지원하는 중…' : '선택한 이력서로 지원하기'}
        </Button>
      </div>
      {error && <p className="small" role="alert">{error}</p>}
    </div>
  );
}
