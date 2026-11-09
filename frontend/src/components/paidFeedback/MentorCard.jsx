import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Button from '../common/Button.jsx';
import { avatarColor } from '../feedback/labels.js';
import './PaidFeedback.css';

// 현직자 목록의 한 사람. 카드를 누르면 상세로, "피드백 요청"은 상세의 요청 칸으로 간다.
export default function MentorCard({ mentor }) {
  const name = mentor.nickname || mentor.username;
  const href = `/mentors/${encodeURIComponent(mentor.id)}`;
  return (
    <article className="mentor-card">
      <Link href={href} className="mentor-card__avatar" aria-label={`${name} 현직자 보기`}>
        <Avatar initial={name.slice(0, 1)} color={avatarColor(mentor.username)} src={mentor.profileImageUrl} size={52} />
      </Link>
      <div className="mentor-card__body">
        <div className="row-wrap" style={{ gap: 6 }}>
          <Link href={href} className="mentor-card__name">{name}</Link>
          <span className="small muted">@{mentor.username}</span>
        </div>
        <div className="row-wrap" style={{ gap: 8 }}>
          <span className="mentor-badge">✓ {mentor.companyName} 현직자</span>
          <span className="small">{mentor.jobTitle}{mentor.careerYears != null ? ` · 경력 ${mentor.careerYears}년` : ''}</span>
        </div>
        <div className="small muted">유료 피드백 <strong className="mentor-card__price">{mentor.priceCredits.toLocaleString('ko-KR')} 크레딧</strong></div>
      </div>
      <Button to={`${href}#request`} variant="primary" size="sm" className="mentor-card__action">피드백 요청</Button>
    </article>
  );
}
