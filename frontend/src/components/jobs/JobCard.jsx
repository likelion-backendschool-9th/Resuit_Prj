import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import './JobCard.css';

// 공고 카드. compact 는 피드 안에 들어가는 작은 형태(회사 줄 생략).
// 일반 공고: 공통 이력서로 바로 지원 / 특화 공고: 상세로 가서 특화 이력서 작성
export default function JobCard({ job, compact = false }) {
  const { applied, toggleApply } = useAppState();
  const special = job.type === 'special';
  const isApplied = !!applied[job.id];

  return (
    <article className={`job-card${special ? ' job-card--special' : ''}`}>
      <div className="job-card__body">
        {!compact && (
          <div className="row-wrap">
            <Avatar initial={job.initial} color={job.av} size={36} company />
            <strong>{job.company}</strong>
            <Badge tone={special ? 'special' : 'general'} size="md">{special ? '특화 공고' : '일반 공고'}</Badge>
            <span className="job-card__dday">{job.dday}</span>
          </div>
        )}
        {compact && (
          <div className="row">
            <Badge tone={special ? 'special' : 'general'} size="md">{special ? '특화 공고' : '일반 공고'}</Badge>
            <span className="xs muted">{special ? `요구 문항 ${job.questions.length}개 · ` : ''}마감 {job.dday}</span>
          </div>
        )}
        <div className="job-card__title">{job.title}</div>
        <div className="small muted">{job.meta}</div>
      </div>
      <div className="job-card__foot">
        <span className="small muted">{special ? '회사 요구 문항에 답한 특화 이력서로 지원' : '공통 이력서를 그대로 제출'}</span>
        {special ? (
          <Button to={`/jobs/${job.id}`}>공고 보고 특화 이력서 작성</Button>
        ) : (
          <Button variant={isApplied ? 'primary' : 'accent-outline'} aria-pressed={isApplied} onClick={() => toggleApply(job.id)}>
            {isApplied ? '지원 완료' : '공통 이력서로 지원'}
          </Button>
        )}
      </div>
    </article>
  );
}
