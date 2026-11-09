import { useEffect, useState } from 'react';
import { getResumePreview } from '../../services/resumes.js';
import Button from '../common/Button.jsx';
import ResumeDocument from '../resume/ResumeDocument.jsx';
import './ResumePreview.css';

// 피드백 게시판 카드 안에서 펼치는 이력서 미리보기 (WBS 4.5). 요청자가 봐 달라고 한 섹션은 강조한다.
// load를 넘기면 그 함수로 이력서를 읽는다(유료 피드백의 요청 이력서처럼 미리보기 API로 볼 수 없는 경우).
export default function ResumePreview({ type, id, requested = [], load }) {
  const [resume, setResume] = useState(null);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setError('');
    (load ? load(controller.signal) : getResumePreview(type, id, controller.signal))
      .then(setResume)
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); });
    return () => controller.abort();
  }, [type, id, retry]);

  if (error) return <div className="resume-preview resume-preview--state" role="alert"><p className="small">{error}</p><Button variant="outline" size="sm" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>;
  if (!resume) return <div className="resume-preview resume-preview--state small muted" role="status">이력서를 불러오는 중이에요.</div>;

  return <ResumeDocument resume={resume} type={type} requested={requested} />;
}
