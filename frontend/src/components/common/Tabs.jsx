import './Tabs.css';

// items: [{ id, label }]   sticky 를 켜면 스크롤해도 화면 위에 붙는다.
export default function Tabs({ items, value, onChange, label, sticky = false }) {
  return (
    <div className={`tabs${sticky ? ' tabs--sticky' : ''}`} role="tablist" aria-label={label}>
      {items.map((it) => (
        <button
          key={it.id}
          type="button"
          role="tab"
          aria-selected={value === it.id}
          className={`tabs__tab${value === it.id ? ' is-active' : ''}`}
          onClick={() => onChange(it.id)}
        >
          {it.label}
        </button>
      ))}
    </div>
  );
}
