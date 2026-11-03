import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import './JobCard.css';

// 공고 카드. compact 는 피드 안에 들어가는 작은 형태(회사 줄 생략, meta · sub 는 게시글이 넘긴다).
// 일반 공고: 상세에서 공통 이력서를 골라 지원 / 특화 공고: 상세에서 특화 이력서 작성
export default function JobCard({ job, compact = false, meta, sub }) {
  const special = job.type === 'special';

  const action = special
    ? <Button to={`/jobs/${job.id}`}>공고 보고 특화 이력서 작성</Button>
    : <Button variant="accent-outline" to={`/jobs/${job.id}`}>공고 보고 공통 이력서로 지원</Button>;

  return (
    <article className={`job-card${special ? ' job-card--special' : ''}${compact ? ' job-card--compact' : ''}`}>
      <div className="job-card__body">
        {compact ? (
          <div className="row">
            <Badge tone={special ? 'special' : 'general'} size="md">{special ? '특화 공고' : '일반 공고'}</Badge>
            <span className="xs muted">{meta}</span>
          </div>
        ) : (
          <div className="row-wrap">
            <Avatar initial={job.initial} color={job.av} size={36} company />
            <strong>{job.company}</strong>
            <Badge tone={special ? 'special' : 'general'} size="md">{special ? '특화 공고' : '일반 공고'}</Badge>
            <span className="job-card__dday">{job.dday}</span>
          </div>
        )}
        <div className="job-card__title">{job.title}</div>
        <div className="small muted">{compact ? sub : job.meta}</div>
      </div>
      <div className="job-card__foot">
        {!compact && <span className="small muted">{special ? '회사 요구 문항에 답한 특화 이력서로 지원' : '공통 이력서를 그대로 제출'}</span>}
        {action}
      </div>
    </article>
  );
}
