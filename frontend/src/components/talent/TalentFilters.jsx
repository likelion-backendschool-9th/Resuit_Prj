import { TALENT_SKILLS } from '../../data/constants.js';
import Badge from '../common/Badge.jsx';
import { ToggleChip } from '../common/Chip.jsx';
import { CAREERS, REGIONS } from './labels.js';

const withAll = (items) => [{ id: 'all', label: '전체' }, ...items];
const EVIDENCE = [
  { key: 'streak', label: '2분기 연속 인증' },
  { key: 'github', label: 'GitHub 1년 기여 500회 이상' },
  { key: 'writer', label: '기술 글 5개 이상 공유' }
];

// 상세 필터. 무료 등급(pro=false)에서는 눌러도 값이 바뀌지 않고 onLocked 만 부른다.
export default function TalentFilters({ pro, filters, onChange, showLockMessage, onLocked }) {
  const guard = (fn) => () => (pro ? fn() : onLocked());
  const badgeOn = pro && filters.badgeOnly;

  return (
    <section className={`card talent-filters${pro ? '' : ' is-locked'}`} aria-labelledby="filter-h" style={{ gap: 14 }}>
      <div className="row" style={{ justifyContent: 'space-between' }}>
        <h2 id="filter-h">상세 필터</h2>
        <Badge tone={pro ? 'accepted' : 'blog'}>{pro ? 'Pro 사용 중' : 'Pro 전용'}</Badge>
      </div>
      {!pro && <p className="small muted">분야별 조회는 무료예요. 아래 필터는 기업 Pro에서 열려요.</p>}
      {!pro && showLockMessage && <p role="alert" className="small" style={{ color: 'var(--special)' }}>기업 Pro 전용 필터예요. 아래에서 체험해 보세요.</p>}

      <button
        type="button"
        role="switch"
        aria-checked={badgeOn}
        className={`talent-switch${badgeOn ? ' is-on' : ''}`}
        onClick={guard(() => onChange({ badgeOnly: !filters.badgeOnly }))}
      >
        <span className="stack" style={{ gap: 2, textAlign: 'left' }}>
          <strong style={{ fontSize: 14 }}>인증 배지 보유자만</strong>
          <span className="xs muted">이번 분기 활성 배지만 · 갱신 필요 제외</span>
        </span>
        <span className="talent-switch__track" aria-hidden="true"><span /></span>
      </button>

      <fieldset className="talent-filters__group">
        <legend>기술 스택</legend>
        <div className="chips" style={{ gap: 6 }}>
          {TALENT_SKILLS.map((k) => (
            <ToggleChip
              key={k}
              size="filter"
              on={pro && filters.skills.includes(k)}
              onClick={guard(() => onChange({ skills: filters.skills.includes(k) ? filters.skills.filter((x) => x !== k) : [...filters.skills, k] }))}
            >
              {k}
            </ToggleChip>
          ))}
        </div>
      </fieldset>

      <fieldset className="talent-filters__group">
        <legend>경력</legend>
        <div className="chips" style={{ gap: 6 }}>
          {withAll(CAREERS).map((c) => (
            <ToggleChip key={c.id} size="filter" on={pro && filters.career === c.id} onClick={guard(() => onChange({ career: c.id }))}>{c.label}</ToggleChip>
          ))}
        </div>
      </fieldset>

      <fieldset className="talent-filters__group">
        <legend>활동 근거</legend>
        {EVIDENCE.map((ev) => {
          const on = pro && !!filters[ev.key];
          return (
            <button key={ev.key} type="button" role="checkbox" aria-checked={on} className="talent-check" onClick={guard(() => onChange({ [ev.key]: !filters[ev.key] }))}>
              <span className={`talent-check__box${on ? ' is-on' : ''}`} aria-hidden="true">{on ? '✓' : ''}</span>{ev.label}
            </button>
          );
        })}
      </fieldset>

      <fieldset className="talent-filters__group">
        <legend>지역</legend>
        <div className="chips" style={{ gap: 6 }}>
          {withAll(REGIONS).map((r) => (
            <ToggleChip key={r.id} size="filter" on={pro && filters.region === r.id} onClick={guard(() => onChange({ region: r.id }))}>{r.label}</ToggleChip>
          ))}
        </div>
      </fieldset>
    </section>
  );
}
