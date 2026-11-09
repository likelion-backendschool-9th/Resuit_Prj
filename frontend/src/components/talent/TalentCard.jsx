import { useState } from 'react';
import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import { avatarColor } from '../feedback/labels.js';
import { CAREER_LABEL, FIELD_LABEL, REGION_LABEL } from './labels.js';

const month = (value) => (value ? value.slice(0, 7) : '');

// 배지 하나를 화면에 보일 칩들로 바꾼다. 활성 배지에는 연속 인증 / 신규 인증 칩이 붙는다.
// streakQuarters는 지난 분기 수라서, 이번 분기를 더해 "연속 n분기"로 보여 준다.
function badgeChips(b) {
  if (b.status === 'GRACE') {
    return [{ tone: 'grace', icon: 'warn', text: `${b.name} · 갱신 필요`, title: '기준 미달로 1분기 유예 중 · 기업 배지 필터에서 제외' }];
  }
  if (b.status !== 'ACTIVE') return [];
  return [
    { tone: 'active', icon: 'check', text: b.name, title: `${month(b.continuousSince || b.acquiredAt)}부터 인증` },
    b.streakQuarters > 0
      ? { tone: 'streak', icon: 'trend', text: `연속 ${b.streakQuarters + 1}분기`, title: `${month(b.continuousSince)}부터 ${b.streakQuarters + 1}분기 연속 갱신` }
      : { tone: 'new', icon: 'plus', text: '신규 인증', title: '이번 분기에 처음 인증' }
  ].map((c) => ({ ...c, key: `${b.id}-${c.tone}` }));
}

// 지난 배지 기간을 칩으로 바꾼다. 이력이 많을 수 있어 최근 2개만 보여 준다.
function historyChips(history) {
  return history.slice(0, 2).map((h, i) => ({
    key: `h-${h.badgeId}-${i}`,
    tone: 'expired',
    icon: 'minus',
    text: `지난 배지 · ${h.badgeName} (${month(h.activeFrom)}~${month(h.activeUntil)})`,
    title: '만료된 배지 · 이력으로만 표시'
  }));
}

// 인재 찾기 목록의 한 사람. 공개 이력서를 펼쳐 보고 채용 제안을 보낼 수 있다.
// loadResume, sendOffer는 페이지에서 받는다. 카드는 API 호출 방식을 모른다.
export default function TalentCard({ person, loadResume, sendOffer }) {
  const { user } = person;
  const name = user.nickname || user.username;
  const profileHref = `/${encodeURIComponent(user.username)}`;
  // 이력서 펼침과 불러온 이력서(처음 펼칠 때 한 번만 부른다)
  const [open, setOpen] = useState(false);
  const [resume, setResume] = useState(null);
  const [resumeError, setResumeError] = useState('');
  const [composing, setComposing] = useState(false);
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  // 이미 제안을 보낸 회원이면 처음부터 "제안 보냄"으로 보여 준다. 제안은 한 번만 보낼 수 있다.
  const [offered, setOffered] = useState(Boolean(person.offered));
  const [offerError, setOfferError] = useState('');

  const chips = [...person.badges.flatMap(badgeChips), ...historyChips(person.badgeHistory)];
  const projects = (resume?.content?.projects || []).filter((p) => p.selected !== false);

  async function toggleResume() {
    const next = !open;
    setOpen(next);
    if (!next || resume) return;
    setResumeError('');
    try {
      setResume(await loadResume(person));
    } catch (err) {
      setResumeError(err.message);
    }
  }

  async function submitOffer(event) {
    event.preventDefault();
    if (!message.trim()) {
      setOfferError('제안 메시지를 입력해 주세요.');
      return;
    }
    setSending(true); setOfferError('');
    try {
      await sendOffer(person, message.trim());
      setOffered(true);
      setComposing(false);
    } catch (err) {
      // 이미 제안을 보낸 회원이면(409) 제안 보냄으로 바꾼다.
      if (err.status === 409) {
        setOffered(true);
        setComposing(false);
        return;
      }
      setOfferError(err.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <article className="talent-card">
      {/* 프로필 사진과 이름을 누르면 그 회원의 프로필(/아이디)로 이동한다. */}
      <Link href={profileHref} className="talent-card__avatar" aria-label={`${name} 프로필 보기`}>
        <Avatar initial={name.slice(0, 1)} color={avatarColor(user.username)} src={user.profileImageUrl} size={48} />
      </Link>
      <div className="talent-card__body">
        <div className="row-wrap" style={{ gap: 6 }}>
          <Link href={profileHref} className="talent-card__name">{name}</Link>
          <span className="small muted">@{user.username}</span>
          <Badge tone="general">{[FIELD_LABEL[person.field], CAREER_LABEL[person.career]].filter(Boolean).join(' · ')}</Badge>
          <span className="xs muted" style={{ marginLeft: 'auto' }}>{person.updatedAt?.slice(0, 10)} 업데이트</span>
        </div>
        <div className="bold">{person.resumeTitle}</div>
        {chips.length > 0 && (
          <div className="row-wrap" style={{ gap: 6 }}>
            {chips.map((c) => (
              <span key={c.key} title={c.title} className={`talent-badge talent-badge--${c.tone}`}><Icon name={c.icon} size={12} />{c.text}</span>
            ))}
          </div>
        )}
        {person.skills.length > 0 && (
          <div className="row-wrap" style={{ gap: 6 }}>
            {person.skills.map((s) => <span key={s} className="talent-skill">{s}</span>)}
          </div>
        )}
        <div className="talent-card__stats">
          <span>GitHub 1년 기여 <strong>{person.contributions.toLocaleString('ko-KR')}</strong></span>
          <span>기술 글 <strong>{person.posts}</strong>개</span>
          {person.region && <span>{REGION_LABEL[person.region] || person.region}</span>}
        </div>
        <div className="row-wrap">
          <Button variant="outline" size="mid" className="talent-card__action" aria-expanded={open} onClick={toggleResume}>
            {open ? '이력서 접기' : '이력서 보기'}
          </Button>
          <Button
            variant={offered ? 'accent-outline' : 'primary'}
            size="mid"
            className="talent-card__action"
            aria-expanded={composing}
            disabled={offered}
            onClick={() => { setComposing((v) => !v); setOfferError(''); }}
          >
            {offered ? '제안 보냄' : '채용 제안'}
          </Button>
        </div>
        {composing && !offered && (
          <form className="talent-card__offer" onSubmit={submitOffer}>
            <label className="xs muted" htmlFor={`offer-${person.resumeId}`}>{name} 님에게 보낼 메시지</label>
            <textarea
              id={`offer-${person.resumeId}`}
              className="textarea"
              rows={3}
              maxLength={2000}
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="회사 소개와 제안하는 포지션을 적어 주세요."
            />
            {offerError && <p role="alert" className="small talent-card__error">{offerError}</p>}
            <div className="row-wrap">
              <Button type="submit" variant="primary" size="mid" className="talent-card__action" disabled={sending}>{sending ? '보내는 중…' : '보내기'}</Button>
              <Button type="button" variant="outline" size="mid" className="talent-card__action" onClick={() => setComposing(false)}>취소</Button>
            </div>
          </form>
        )}
        {open && (
          <div className="talent-card__resume">
            <div className="xs muted">공개 이력서 · {person.resumeTitle}</div>
            {!resume && !resumeError && <div className="small muted">이력서를 불러오는 중…</div>}
            {resumeError && <p role="alert" className="small talent-card__error">{resumeError}</p>}
            {resume && projects.length === 0 && <div className="small muted">등록한 프로젝트가 없어요.</div>}
            {projects.map((p, i) => (
              <div key={`${p.title}-${i}`} style={{ fontSize: 14 }}><strong>{p.title}</strong>{p.desc ? ` — ${p.desc}` : ''}</div>
            ))}
            {person.activity && (
              <div className="small" style={{ color: 'var(--accent-text)' }}>
                배지 근거 · 최근 2분기 채택 {person.activity.adoptedCount}건 · 서로 다른 요청자 {person.activity.distinctRequesters}명 · 베스트 리뷰어 선정 {person.activity.bestReviewCount}회
              </div>
            )}
          </div>
        )}
      </div>
    </article>
  );
}
