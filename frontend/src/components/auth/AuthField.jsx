import { useState } from 'react';
import Icon from '@/components/common/Icon.jsx';

export default function AuthField({ label, hint, error, type = 'text', name, ...props }) {
  const [visible, setVisible] = useState(false);
  const password = type === 'password';
  const id = `auth-${name}`;
  return (
    <div className="field auth-field">
      <label htmlFor={id}>{label}</label>
      <div className="auth-field__control">
        <input {...props} id={id} name={name} className="input" type={password && visible ? 'text' : type}
          aria-invalid={error ? true : undefined}
          aria-describedby={[hint && `${id}-hint`, error && `${id}-error`].filter(Boolean).join(' ') || undefined} />
        {password && <button type="button" className="auth-field__visibility" onClick={() => setVisible(!visible)}
          aria-label={`${label} ${visible ? '숨기기' : '표시'}`} aria-pressed={visible} disabled={props.disabled}>
          <Icon name="eye" size={19} /><span>{visible ? '숨김' : '표시'}</span>
        </button>}
      </div>
      {hint && <p className="auth-field__hint" id={`${id}-hint`}>{hint}</p>}
      {error && <p className="auth-field__error" id={`${id}-error`} role="alert">{error}</p>}
    </div>
  );
}
