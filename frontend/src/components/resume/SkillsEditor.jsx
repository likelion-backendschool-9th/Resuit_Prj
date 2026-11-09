'use client';

import { useState } from 'react';
import Button, { IconButton } from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import './ResumeFields.css';

export const MAX_SKILLS = 30;
export const MAX_SKILL_LENGTH = 30;

// 기술 스택 편집: 입력하고 Enter나 추가를 누르면 칩이 생기고, 칩의 X로 지운다. 같은 기술은 한 번만 넣는다.
export default function SkillsEditor({ skills, onChange }) {
  const [draft, setDraft] = useState('');
  const [notice, setNotice] = useState('');
  const full = skills.length >= MAX_SKILLS;

  function add() {
    const name = draft.trim();
    if (!name) return;
    if (skills.some((s) => s.toLowerCase() === name.toLowerCase())) {
      setNotice(`「${name}」 기술은 이미 넣었어요.`);
      return;
    }
    onChange([...skills, name]);
    setDraft('');
    setNotice('');
  }

  return (
    <div className="stack" style={{ gap: 10 }}>
      {skills.length > 0 && (
        <div className="row-wrap" style={{ gap: 6 }}>
          {skills.map((s) => (
            <span key={s} className="resume-skill resume-skill--edit">
              {s}
              <IconButton label={`${s} 삭제`} className="resume-skill__remove" onClick={() => onChange(skills.filter((x) => x !== s))}><Icon name="x" size={14} /></IconButton>
            </span>
          ))}
        </div>
      )}
      <div className="resume-skill-add">
        <label htmlFor="skill-input" className="sr-only">추가할 기술</label>
        <input
          id="skill-input" className="input" maxLength={MAX_SKILL_LENGTH} disabled={full}
          placeholder={full ? `기술은 ${MAX_SKILLS}개까지 넣을 수 있어요` : '예: React, Spring Boot'}
          value={draft}
          onChange={(e) => { setDraft(e.target.value); setNotice(''); }}
          onKeyDown={(e) => {
            // 한글 조합 중 Enter는 글자 확정이므로 추가하지 않는다
            if (e.key === 'Enter' && !e.nativeEvent.isComposing) { e.preventDefault(); add(); }
          }}
        />
        <Button variant="outline" onClick={add} disabled={full || !draft.trim()}>추가</Button>
      </div>
      <div className="xs muted" role={notice ? 'alert' : undefined}>{notice || `${skills.length} / ${MAX_SKILLS}개`}</div>
    </div>
  );
}
