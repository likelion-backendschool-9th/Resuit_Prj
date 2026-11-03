import { useState } from 'react';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';

// 배지 하나를 화면에 보일 칩들로 바꾼다. 활성 배지에는 연속 인증 / 신규 인증 칩이 붙는다.
function badgeChips(b) {
  if (b.status === 'grace') {
    return [{ tone: 'grace', icon: 'warn', text: `${b.label} · 갱신 필요`, title: '기준 미달로 1분기 유예 중 · 기업 배지 필터에서 제외' }];
  }
  if (b.status === 'expired') {
    return [{ tone: 'expired', icon: 'minus', text: `지난 배지 · ${b.label} (${b.since}~${b.until || ''})`, title: '만료된 배지 · 이력으로만 표시' }];
  }
  return [
    { tone: 'active', icon: 'check', text: b.label, title: `${b.since}부터 인증` },
    b.streak > 1
      ? { tone: 'streak', icon: 'trend', text: `연속 ${b.streak}분기`, title: `${b.since}부터 ${b.streak}분기 연속 갱신` }
      : { tone: 'new', icon: 'plus', text: '신규 인증', title: '이번 분기에 처음 인증' }
  ];
}

// 인재 찾기 목록의 한 사람. 공개 이력서를 펼쳐 보고 채용 제안을 보낼 수 있다.
export default function TalentCard({ person }) {
  const [open, setOpen] = useState(false);
  const [offered, setOffered] = useState(false);
  const chips = person.badges.flatMap(badgeChips);

  return (
    <article className="talent-card">
      <Avatar initial={person.initial} color={person.av} size={48} />
      <div className="talent-card__body">
        <div className="row-wrap" style={{ gap: 6 }}>
          <strong style={{ fontSize: 16 }}>{person.name}</strong>
          <span className="small muted">@{person.handle}</span>
          <Badge tone="general">{person.field} · {person.career}</Badge>
          <span className="xs muted" style={{ marginLeft: 'auto' }}>{person.updated} 업데이트</span>
        </div>
        <div className="bold">{person.headline}</div>
        {chips.length > 0 && (
          <div className="row-wrap" style={{ gap: 6 }}>
            {chips.map((c) => (
              <span key={c.text} title={c.title} className={`talent-badge talent-badge--${c.tone}`}><Icon name={c.icon} size={12} />{c.text}</span>
            ))}
          </div>
        )}
        <div className="row-wrap" style={{ gap: 6 }}>
          {person.skills.map((s) => <span key={s} className="talent-skill">{s}</span>)}
        </div>
        <div className="talent-card__stats">
          <span>GitHub 1년 기여 <strong>{person.github.toLocaleString('ko-KR')}</strong></span>
          <span>기술 글 <strong>{person.posts}</strong>개</span>
          <span>{person.region}</span>
        </div>
        <div className="row-wrap">
          <Button variant="outline" size="mid" className="talent-card__action" aria-expanded={open} onClick={() => setOpen((v) => !v)}>
            {open ? '이력서 접기' : '이력서 보기'}
          </Button>
          <Button variant={offered ? 'accent-outline' : 'primary'} size="mid" className="talent-card__action" aria-pressed={offered} onClick={() => setOffered((v) => !v)}>
            {offered ? '제안 보냄' : '채용 제안'}
          </Button>
        </div>
        {open && (
          <div className="talent-card__resume">
            <div className="xs muted">공개 이력서 · {person.resumeTitle}</div>
            {person.projects.map(([name, desc]) => (
              <div key={name} style={{ fontSize: 14 }}><strong>{name}</strong> — {desc}</div>
            ))}
            {person.badges.length > 0 && (
              <div className="small" style={{ color: 'var(--accent-text)' }}>
                배지 근거 · 최근 2분기 채택 {person.recent}건 · 서로 다른 요청자 {person.requesters}명 · 베스트 리뷰어 선정 {person.best}회
              </div>
            )}
          </div>
        )}
      </div>
    </article>
  );
}
