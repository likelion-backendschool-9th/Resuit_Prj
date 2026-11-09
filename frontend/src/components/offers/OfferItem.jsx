import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import { avatarColor } from '../feedback/labels.js';
import './OfferItem.css';

// 받은 채용 제안 하나. 보낸 회사, 받은 날짜, 연결된 공고, 메시지를 보여 준다.
export default function OfferItem({ offer }) {
  const { company, job } = offer;
  const name = company.nickname || company.username;

  return (
    <article className="offer-item">
      <Avatar initial={name.slice(0, 1)} color={avatarColor(company.username)} src={company.profileImageUrl} size={44} company />
      <div className="offer-item__body">
        <div className="row-wrap" style={{ gap: 6 }}>
          <Link href={`/${encodeURIComponent(company.username)}`} className="offer-item__company">{name}</Link>
          <span className="small muted">@{company.username}</span>
          <Badge tone="accent">채용 제안</Badge>
          <span className="xs muted offer-item__date">{offer.createdAt?.slice(0, 10)}</span>
        </div>
        {job && <Link href={`/jobs/${job.id}`} className="small offer-item__job">공고 · {job.title}</Link>}
        <p className="offer-item__message">{offer.message}</p>
      </div>
    </article>
  );
}
