import { useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import Button from '@/components/common/Button.jsx';
import { IconButton } from '@/components/common/Button.jsx';
import Icon from '@/components/common/Icon.jsx';
import { createJob, updateJob, createJobDraft, updateJobDraft } from '@/services/jobs.js';

const empty = { type: 'GENERAL', title: '', location: '', employmentType: '', career: '', description: '', requirements: '', preferred: '', deadline: '', questions: [] };
const textFields = [['title', '공고 제목', 100], ['location', '근무 지역', 50], ['employmentType', '고용 형태', 30], ['career', '경력 조건', 30]];

export default function JobEditor({ job, onSaved }) {
  const router = useRouter();
  const [values, setValues] = useState(() => ({ ...empty, ...job, deadline: job?.deadline || '', questions: (job?.questions || []).map((q, index) => ({ ...q, key: index })) }));
  const [savedId, setSavedId] = useState(job?.id);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const pending = useRef(false);
  const sequence = useRef(values.questions.length);
  const published = job && job.status !== 'DRAFT';
  const change = (name, value) => setValues((old) => ({ ...old, [name]: value }));
  const changeQuestion = (key, name, value) => change('questions', values.questions.map((q) => q.key === key ? { ...q, [name]: value } : q));

  async function submit(event) {
    event.preventDefault();
    if (pending.current) return;
    const draft = event.nativeEvent.submitter?.value === 'draft';
    setError(''); setMessage('');
    if (!values.title.trim()) { setError('공고 제목을 입력해 주세요.'); return; }
    const questions = values.type === 'SPECIAL' ? values.questions.map((q) => ({ content: q.content.trim(), charLimit: q.charLimit === '' || q.charLimit == null ? null : Number(q.charLimit) })) : [];
    if (!draft && values.type === 'SPECIAL' && (!questions.length || questions.some((q) => !q.content))) {
      setError('특화 공고에는 내용이 있는 요구 문항을 하나 이상 입력해 주세요.'); return;
    }
    const body = Object.fromEntries(Object.keys(empty).map((key) => [key, values[key] ?? '']));
    body.title = values.title.trim(); body.deadline = values.deadline || null; body.questions = questions;
    pending.current = true; setBusy(true);
    try {
      if (draft) {
        const saved = savedId ? await updateJobDraft(savedId, body) : await createJobDraft(body);
        setSavedId(saved.id); setMessage('임시저장했어요. 임시저장 목록에서 이어서 작성할 수 있어요.');
        onSaved();
        router.replace(`/company/post?draft=${saved.id}`, { scroll: false });
      } else {
        const saved = savedId ? await updateJob(savedId, body) : await createJob(body);
        router.push(`/jobs/${saved.id}`); router.refresh();
      }
    } catch (e) { setError(e.message); }
    finally { pending.current = false; setBusy(false); }
  }

  return <form className="job-editor" onSubmit={submit} aria-busy={busy}>
    <fieldset disabled={busy}>
      <legend className="sr-only">공고 내용</legend>
      <div className="field"><span className="bold">공고 유형</span><div className="grid-2" role="radiogroup" aria-label="공고 유형">
        <button type="button" role="radio" aria-checked={values.type === 'GENERAL'} className={`choice${values.type === 'GENERAL' ? ' is-on' : ''}`} onClick={() => change('type', 'GENERAL')}><span className="choice__title">일반 공고</span><span className="choice__desc">지원자가 공통 이력서를 그대로 제출</span></button>
        <button type="button" role="radio" aria-checked={values.type === 'SPECIAL'} className={`choice${values.type === 'SPECIAL' ? ' is-on' : ''}`} onClick={() => change('type', 'SPECIAL')}><span className="choice__title">특화 공고</span><span className="choice__desc">원하는 문항을 받아 특화 이력서로 지원</span></button>
      </div></div>
      <div className="field"><label htmlFor="job-title">공고 제목 (필수)</label><input id="job-title" className="input" value={values.title || ''} onChange={(e) => change('title', e.target.value)} maxLength={100} required /></div>
      <div className="grid-2">{textFields.slice(1).map(([name, label, max]) => <div className="field" key={name}><label htmlFor={`job-${name}`}>{label}</label><input id={`job-${name}`} className="input" value={values[name] || ''} onChange={(e) => change(name, e.target.value)} maxLength={max} /></div>)}
        <div className="field"><label htmlFor="job-deadline">마감일</label><input id="job-deadline" type="date" className="input" value={values.deadline} onChange={(e) => change('deadline', e.target.value)} /></div>
      </div>
      {[['description', '상세 내용'], ['requirements', '자격 요건'], ['preferred', '우대 사항']].map(([name, label]) => <div className="field" key={name}><label htmlFor={`job-${name}`}>{label}</label><textarea id={`job-${name}`} className="textarea" rows={5} value={values[name] || ''} onChange={(e) => change(name, e.target.value)} /></div>)}
      {values.type === 'SPECIAL' && <section className="stack"><h2 className="section-title">요구 문항</h2><p className="small muted">게시하려면 문항이 하나 이상 필요합니다. 입력한 순서대로 표시됩니다.</p>
        {values.questions.map((q, index) => <div key={q.key} className="job-editor__question">
          <span className="q-item__num">{index + 1}</span><div className="field"><label className="sr-only" htmlFor={`job-question-${q.key}`}>문항 {index + 1}</label><input id={`job-question-${q.key}`} className="q-item__text" maxLength={300} value={q.content} placeholder="새 문항을 입력하세요" onChange={(e) => changeQuestion(q.key, 'content', e.target.value)} /></div>
          <div className="field job-editor__limit"><label className="sr-only" htmlFor={`job-limit-${q.key}`}>답변 글자 수 제한</label><input id={`job-limit-${q.key}`} className="input" type="number" min={1} max={2147483647} step={1} placeholder="제한 없음" value={q.charLimit ?? ''} onChange={(e) => changeQuestion(q.key, 'charLimit', e.target.value)} /></div>
          <IconButton label="문항 삭제" className="icon-btn--plain icon-btn--sm q-item__remove" onClick={() => change('questions', values.questions.filter((item) => item.key !== q.key))}><Icon name="x" size={16} /></IconButton>
        </div>)}
        <button type="button" className="q-box__add" onClick={() => change('questions', [...values.questions, { key: sequence.current++, content: '', charLimit: 500 }])}>문항 추가</button>
      </section>}
    </fieldset>
    {error && <p role="alert" className="job-editor__error">{error}</p>}
    {message && <p role="status" className="job-editor__message">{message}</p>}
    <div className="job-editor__actions">
      {!published && <Button type="submit" name="action" value="draft" variant="outline" disabled={busy}>임시저장</Button>}
      <Button type="submit" name="action" value="publish" disabled={busy}>{busy ? '저장 중…' : published ? '수정 완료' : '공고 게시'}</Button>
    </div>
  </form>;
}
