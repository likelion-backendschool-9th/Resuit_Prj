'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Button from '../common/Button.jsx';
import Badge from '../common/Badge.jsx';
import { restoreSubmittedResume } from '../../services/resumes.js';
import './SubmittedResumeImports.css';

export default function SubmittedResumeImports({ submissions }) {
  const router = useRouter();
  const [pending, setPending] = useState(null);
  const [error, setError] = useState('');
  async function restore(id) {
    setPending(id);
    setError('');
    try {
      const result = await restoreSubmittedResume(id);
      router.push(result.resumeType === 'COMMON' ? `/resumes/common/edit?id=${result.id}` : `/resumes/edit?id=${result.id}`);
    } catch (failure) { setError(failure.message); setPending(null); }
  }
  return (
    <details className="submitted-imports section">
      <summary className="section-label">제출 이력서 불러오기 · {submissions.length}개</summary>
      <p className="small muted">지원 당시 내용과 사진으로 새 초안을 만듭니다. 지원일로부터 3개월 동안 불러올 수 있어요.</p>
      {error && <p className="small" role="alert">{error}</p>}
      {submissions.length === 0 ? <div className="empty">불러올 수 있는 제출 이력서가 없어요.</div>
        : submissions.map((item) => (
          <article key={item.applicationId} className="submitted-imports__item">
            <div className="submitted-imports__info">
              <strong>{item.restoreTitle}</strong>
              <span className="small muted">보존 기한 {item.expiresAt.replace('T', ' ').slice(0, 16)}</span>
              <Badge tone={item.resumeType === 'COMMON' ? 'general' : 'special'}>{item.resumeType === 'COMMON' ? '공통' : '특화'}</Badge>
            </div>
            <Button variant="outline" size="mid" disabled={pending !== null} onClick={() => restore(item.applicationId)}>
              {pending === item.applicationId ? '불러오는 중' : '새 초안으로 불러오기'}
            </Button>
          </article>
        ))}
    </details>
  );
}
