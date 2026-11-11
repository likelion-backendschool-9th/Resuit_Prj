import Badge from '../common/Badge.jsx';
import './ResumeDocument.css';

const SECTION_LABEL = { BASIC: '기본 정보', SKILLS: '기술 스택', PROJECTS: '프로젝트', AWARDS: '수상', INTRODUCTION: '자기소개', ANSWERS: '문항 답변' };

const BASIC_FIELDS = [['name', '이름'], ['school', '학력'], ['github', 'GitHub']];

const isEmpty = (value) => value == null || (Array.isArray(value) ? value.length === 0 : typeof value === 'string' ? !value.trim() : false);

// 수상은 문자열이나 { title | name, desc | date } 형태가 섞여 올 수 있다
const awardText = (award) => (typeof award === 'string' ? award : [award.title || award.name, award.desc || award.date].filter(Boolean).join(' · '));


// Read-only document. All content comes from props; this component never fetches the editable source.
export default function ResumeDocument({ resume, type, requested = [] }) {
  const content = resume.content || {};
  const basic = content.basic || {};
  const projects = (content.projects || []).filter((p) => type !== 'TARGET' || p.selected !== false);
  const answers = Object.fromEntries((content.answers || []).map((a) => [a.questionId, a.answer]));

  const block = (key, body, empty) => (
    <section key={key} className={`resume-document__block${requested.includes(key) ? ' is-requested' : ''}`}>
      <div className="resume-document__head">
        <h3>{SECTION_LABEL[key]}</h3>
        {requested.includes(key) && <Badge tone="focus">봐 줬으면 하는 부분</Badge>}
      </div>
      {empty ? <span className="small muted">작성하지 않았어요</span> : body}
    </section>
  );

  return (
    <div className="resume-document">
      {resume.photoUrl && <img className="resume-document__photo" src={resume.photoUrl} alt="이력서 사진" />}
      {block('BASIC', (
        <dl className="resume-document__basic">
          {BASIC_FIELDS.filter(([k]) => !isEmpty(basic[k])).map(([k, label]) => (
            <div key={k}><dt className="xs muted">{label}</dt><dd>{basic[k]}</dd></div>
          ))}
        </dl>
      ), BASIC_FIELDS.every(([k]) => isEmpty(basic[k])))}

      {block('SKILLS', (
        <div className="row-wrap">{(content.skills || []).map((s) => <span key={s} className="resume-document__skill">{s}</span>)}</div>
      ), isEmpty(content.skills))}

      {block('PROJECTS', projects.map((p, i) => (
        <div key={p.id ?? i}>
          <div className="bold">{p.title}</div>
          {p.desc && <p className="small muted">{p.desc}</p>}
        </div>
      )), projects.length === 0)}

      {block('AWARDS', (
        <ul className="resume-document__list">{(content.awards || []).map((a, i) => <li key={i}>{awardText(a)}</li>)}</ul>
      ), isEmpty(content.awards))}

      {block('INTRODUCTION', <p className="resume-document__text">{content.introduction}</p>, isEmpty(content.introduction))}

      {type === 'TARGET' && block('ANSWERS', resume.questions.map((q) => (
        <div key={q.id}>
          <div className="small bold">{q.orderNo}. {q.content}</div>
          <p className="resume-document__text">{answers[q.id] || <span className="muted">답변하지 않았어요</span>}</p>
        </div>
      )), resume.questions.length === 0)}
    </div>
  );
}
