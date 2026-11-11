import { useState } from 'react';
import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import { avatarColor } from '../feedback/labels.js';
import './OfferItem.css';

// 제안 상태별 배지. 받은 제안과 보낸 제안이 같은 문구를 쓴다.
const STATUS = {
  SENT: { tone: 'general', label: '응답 대기' },
  ACCEPTED: { tone: 'accepted', label: '승낙' },
  DECLINED: { tone: 'special', label: '거절' }
};

// 채용 제안 하나. 받은 제안(box=received)은 보낸 회사를, 보낸 제안(box=sent)은 받은 회원을 보여 준다.
// 받은 제안이 아직 응답 대기(SENT)면 승낙·거절 버튼을 보여 준다. 응답 방식(onRespond)은 페이지에서 받는다.
export default function OfferItem({ offer, box = 'received', onRespond }) {
  const sent = box === 'sent';
  const person = sent ? offer.user : offer.company;
  const { job } = offer;
  const name = person.nickname || person.username;
  const status = STATUS[offer.status] || { tone: 'general', label: offer.status };
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function respond(next) {
    const label = next === 'ACCEPTED' ? '승낙' : '거절';
    if (!window.confirm(`${name}의 채용 제안을 ${label}할까요? 응답은 바꿀 수 없어요.`)) return;
    setBusy(true); setError('');
    try {
      await onRespond(offer, next);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <article className="offer-item">
      <Avatar initial={name.slice(0, 1)} color={avatarColor(person.username)} src={person.profileImageUrl} size={44} company={!sent} />
      <div className="offer-item__body">
        <div className="row-wrap" style={{ gap: 6 }}>
          <Link href={`/${encodeURIComponent(person.username)}`} className="offer-item__company">{name}</Link>
          <span className="small muted">@{person.username}</span>
          <Badge tone={status.tone}>{status.label}</Badge>
          <span className="xs muted offer-item__date">{offer.createdAt?.slice(0, 10)}</span>
        </div>
        {!sent && offer.companyEmail && (
          <a href={`mailto:${offer.companyEmail}`} className="small offer-item__email">{offer.companyEmail}</a>
        )}
        {job && <Link href={`/jobs/${job.id}`} className="small offer-item__job">공고 · {job.title}</Link>}
        <p className="offer-item__message">{offer.message}</p>
        {!sent && offer.status === 'SENT' && onRespond && (
          <div className="row-wrap offer-item__actions">
            <Button variant="primary" size="sm" onClick={() => respond('ACCEPTED')} disabled={busy}>승낙</Button>
            <Button variant="outline" size="sm" onClick={() => respond('DECLINED')} disabled={busy}>거절</Button>
          </div>
        )}
        {error && <p role="alert" className="small offer-item__error">{error}</p>}
      </div>
    </article>
  );
}
