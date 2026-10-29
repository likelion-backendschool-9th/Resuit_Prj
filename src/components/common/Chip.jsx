import './Chip.css';

// 하나만 고르는 칩 묶음 (radio)
export function ChipRadioGroup({ options, value, onChange, label, tone = 'accent', size = 'md' }) {
  return (
    <div className="chips" role="radiogroup" aria-label={label}>
      {options.map((o) => (
        <button
          key={o.id}
          type="button"
          role="radio"
          aria-checked={value === o.id}
          className={`chip chip--${tone} chip--${size}${value === o.id ? ' is-on' : ''}`}
          onClick={() => onChange(o.id)}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}

// 켜고 끄는 칩 하나 (toggle)
export function ToggleChip({ on, onClick, tone = 'accent', size = 'md', children }) {
  return (
    <button type="button" aria-pressed={on} className={`chip chip--${tone} chip--${size}${on ? ' is-on' : ''}`} onClick={onClick}>
      {children}
    </button>
  );
}
