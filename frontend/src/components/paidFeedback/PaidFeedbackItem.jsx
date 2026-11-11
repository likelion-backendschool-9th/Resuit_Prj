import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import { avatarColor } from '../feedback/labels.js';
import { dateText, statusOf } from './labels.js';
import './PaidFeedback.css';

// 내 유료 피드백 목록의 한 줄. 보낸 요청(box=sent)은 현직자를, 받은 요청(box=received)은 요청자를 보여 준다.
export default function PaidFeedbackItem({ item, box }) {
  const sent = box === 'sent';
  const person = sent ? item.mentor : item.requester;
  const name = person.nickname || person.username;
  const status = statusOf(item.status);
  return (
    <Link href={`/paid-feedback/${item.id}`} className="paid-item">
      <Avatar initial={name.slice(0, 1)} color={avatarColor(person.username)} src={person.profileImageUrl} size={44} />
      <div className="paid-item__body">
        <strong>{sent ? `${name} 현직자에게` : `${name} 님의 요청`}</strong>
        <span className="small">이력서 · {item.resumeTitle}</span>
      </div>
      <div className="paid-item__meta">
        <Badge tone={status.tone}>{status.label}</Badge>
        <span className="xs muted">{dateText(item.createdAt)}</span>
        <span className="small paid-item__credits">{sent ? '-' : '+'}{item.credits.toLocaleString('ko-KR')} 크레딧</span>
      </div>
    </Link>
  );
}
