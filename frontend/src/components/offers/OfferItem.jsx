import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import { avatarColor } from '../feedback/labels.js';
import './OfferItem.css';

// 채용 제안 하나. 받은 제안(box=received)은 보낸 회사를, 보낸 제안(box=sent)은 받은 회원을 보여 준다.
export default function OfferItem({ offer, box = 'received' }) {
  const sent = box === 'sent';
  const person = sent ? offer.user : offer.company;
  const { job } = offer;
  const name = person.nickname || person.username;

  return (
    <article className="offer-item">
      <Avatar initial={name.slice(0, 1)} color={avatarColor(person.username)} src={person.profileImageUrl} size={44} company={!sent} />
      <div className="offer-item__body">
        <div className="row-wrap" style={{ gap: 6 }}>
          <Link href={`/${encodeURIComponent(person.username)}`} className="offer-item__company">{name}</Link>
          <span className="small muted">@{person.username}</span>
          <Badge tone="accent">{sent ? '보낸 제안' : '채용 제안'}</Badge>
          <span className="xs muted offer-item__date">{offer.createdAt?.slice(0, 10)}</span>
        </div>
        {job && <Link href={`/jobs/${job.id}`} className="small offer-item__job">공고 · {job.title}</Link>}
        <p className="offer-item__message">{offer.message}</p>
      </div>
    </article>
  );
}
