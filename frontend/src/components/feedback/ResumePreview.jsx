import { useEffect, useState } from 'react';
import { getResumePreview } from '../../services/resumes.js';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import { SECTION_LABEL } from './labels.js';
import './ResumePreview.css';

const BASIC_FIELDS = [['name', '이름'], ['school', '학력'], ['github', 'GitHub']];

const isEmpty = (value) => value == null || (Array.isArray(value) ? value.length === 0 : typeof value === 'string' ? !value.trim() : false);

// 수상은 문자열이나 { title | name, desc | date } 형태가 섞여 올 수 있다
const awardText = (award) => (typeof award === 'string' ? award : [award.title || award.name, award.desc || award.date].filter(Boolean).join(' · '));

// 피드백 게시판 카드 안에서 펼치는 이력서 미리보기 (WBS 4.5). 요청자가 봐 달라고 한 섹션은 강조한다.
export default function ResumePreview({ type, id, requested = [] }) {
  const [resume, setResume] = useState(null);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setError('');
    getResumePreview(type, id, controller.signal)
      .then(setResume)
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); });
    return () => controller.abort();
  }, [type, id, retry]);

  if (error) return <div className="resume-preview resume-preview--state" role="alert"><p className="small">{error}</p><Button variant="outline" size="sm" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>;
  if (!resume) return <div className="resume-preview resume-preview--state small muted" role="status">이력서를 불러오는 중이에요.</div>;

  const content = resume.content || {};
  const basic = content.basic || {};
  const projects = (content.projects || []).filter((p) => type !== 'TARGET' || p.selected !== false);
  const answers = Object.fromEntries((content.answers || []).map((a) => [a.questionId, a.answer]));

  const block = (key, body, empty) => (
    <section key={key} className={`resume-preview__block${requested.includes(key) ? ' is-requested' : ''}`}>
      <div className="resume-preview__head">
        <h3>{SECTION_LABEL[key]}</h3>
        {requested.includes(key) && <Badge tone="focus">봐 줬으면 하는 부분</Badge>}
      </div>
      {empty ? <span className="small muted">작성하지 않았어요</span> : body}
    </section>
  );

  return (
    <div className="resume-preview">
      {block('BASIC', (
        <div className="resume-preview__basic-row">
          {resume.photoUrl && <img className="resume-preview__photo" src={resume.photoUrl} alt="이력서 사진" />}
          <dl className="resume-preview__basic">
            {BASIC_FIELDS.filter(([k]) => !isEmpty(basic[k])).map(([k, label]) => (
              <div key={k}><dt className="xs muted">{label}</dt><dd>{basic[k]}</dd></div>
            ))}
          </dl>
        </div>
      ), !resume.photoUrl && BASIC_FIELDS.every(([k]) => isEmpty(basic[k])))}

      {block('SKILLS', (
        <div className="row-wrap">{(content.skills || []).map((s) => <span key={s} className="resume-preview__skill">{s}</span>)}</div>
      ), isEmpty(content.skills))}

      {block('PROJECTS', projects.map((p, i) => (
        <div key={p.id ?? i}>
          <div className="bold">{p.title}</div>
          {p.desc && <p className="small muted">{p.desc}</p>}
        </div>
      )), projects.length === 0)}

      {block('AWARDS', (
        <ul className="resume-preview__list">{(content.awards || []).map((a, i) => <li key={i}>{awardText(a)}</li>)}</ul>
      ), isEmpty(content.awards))}

      {block('INTRODUCTION', <p className="resume-preview__text">{content.introduction}</p>, isEmpty(content.introduction))}

      {type === 'TARGET' && block('ANSWERS', resume.questions.map((q) => (
        <div key={q.id}>
          <div className="small bold">{q.orderNo}. {q.content}</div>
          <p className="resume-preview__text">{answers[q.id] || <span className="muted">답변하지 않았어요</span>}</p>
        </div>
      )), resume.questions.length === 0)}
    </div>
  );
}
