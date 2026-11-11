'use client';

import './ResumeFields.css';

// 이력서 편집 화면의 한 줄 입력칸. 글자 수가 제한에 가까워지면 남은 수를 보여 준다.
export function TextField({ id, label, value, onChange, max, placeholder, hint }) {
  const length = (value || '').length;
  return (
    <div className="field resume-field">
      <label htmlFor={id}>{label}</label>
      <input
        id={id} className="input" maxLength={max} placeholder={placeholder}
        value={value || ''} onChange={(e) => onChange(e.target.value)}
      />
      {(hint || length >= max * 0.8) && (
        <div className="resume-field__foot xs muted"><span>{hint}</span>{length >= max * 0.8 && <span>{length} / {max}자</span>}</div>
      )}
    </div>
  );
}

// 여러 줄 입력칸. 글자 수를 항상 보여 준다.
export function TextAreaField({ id, label, value, onChange, max, rows = 6, placeholder }) {
  return (
    <div className="field resume-field">
      <label htmlFor={id} className="sr-only">{label}</label>
      <textarea
        id={id} className="textarea" rows={rows} maxLength={max} placeholder={placeholder}
        value={value || ''} onChange={(e) => onChange(e.target.value)}
      />
      <div className="resume-field__foot xs muted"><span /><span>{(value || '').length} / {max}자</span></div>
    </div>
  );
}
