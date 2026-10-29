import './Avatar.css';

// color: tokens.css 의 아바타 토큰 이름 (av1~av6, co1~co4). company 면 둥근 사각형.
export default function Avatar({ initial, color = 'av1', size = 44, company = false }) {
  return (
    <div
      className={`avatar${company ? ' avatar--company' : ''}`}
      style={{ width: size, height: size, flexBasis: size, fontSize: Math.round(size * 0.36), background: `var(--${color}-bg)`, color: `var(--${color}-fg)` }}
      aria-hidden="true"
    >
      {initial}
    </div>
  );
}
