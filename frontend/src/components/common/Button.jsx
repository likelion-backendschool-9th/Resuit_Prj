import { Link } from 'react-router-dom';
import './Button.css';

// variant: primary | outline | accent-outline | dark | ghost
// size: sm | md | lg     to 를 넘기면 링크(Link)로 렌더링된다.
export default function Button({ variant = 'primary', size = 'md', block = false, to, className = '', children, ...rest }) {
  const cls = `btn btn--${variant} btn--${size}${block ? ' btn--block' : ''} ${className}`.trim();
  if (to) {
    return <Link to={to} className={cls} {...rest}>{children}</Link>;
  }
  return <button type="button" className={cls} {...rest}>{children}</button>;
}

export function IconButton({ label, className = '', children, ...rest }) {
  return (
    <button type="button" className={`icon-btn ${className}`.trim()} aria-label={label} title={label} {...rest}>
      {children}
    </button>
  );
}
