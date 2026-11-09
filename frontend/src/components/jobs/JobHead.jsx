import Link from 'next/link';
import Avatar from '@/components/common/Avatar.jsx';
import Badge from '@/components/common/Badge.jsx';

export default function JobHead({ job }) {
  const company = job.company.nickname || job.company.username;
  return <div className="section">
    <div className="row"><Avatar initial={company.slice(0, 1)} src={job.company.profileImageUrl} color="co2" size={48} company /><div><strong>{company}</strong><div><Link className="text-link" href={`/${encodeURIComponent(job.company.username)}`}>@{job.company.username}</Link></div></div></div>
    <div className="row-wrap"><Badge tone={job.type === 'SPECIAL' ? 'special' : 'general'}>{job.type === 'SPECIAL' ? '특화 공고' : '일반 공고'}</Badge><Badge>{job.status === 'CLOSED' ? '마감' : '모집 중'}</Badge></div>
    <h2>{job.title}</h2>
    <p className="muted">{[job.location, job.employmentType, job.career].filter(Boolean).join(' · ') || '상세 조건 미등록'}</p>
    <p className="small muted">마감일: {job.deadline || '미정'}</p>
    {[['상세 내용', job.description], ['자격 요건', job.requirements], ['우대 사항', job.preferred]].map(([title, body]) => <section key={title} className="jobs-description"><h3>{title}</h3><p className="pre-line">{body || '등록된 내용이 없어요.'}</p></section>)}
    {job.type === 'SPECIAL' && <section className="jobs-description"><h3>요구 문항</h3><ol className="jobs-questions">{(job.questions || []).map((q) => <li key={q.id}><p className="pre-line">{q.content}</p><span className="small muted">{q.charLimit ? `${q.charLimit}자 이내` : '글자 수 제한 없음'}</span></li>)}</ol></section>}
  </div>;
}
