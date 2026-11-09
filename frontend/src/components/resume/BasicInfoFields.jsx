'use client';

import { TextField } from './ResumeFields.jsx';

const BASIC_FIELDS = [
  { key: 'name', label: '이름', max: 30, placeholder: '예: 이서연' },
  { key: 'school', label: '학력', max: 100, placeholder: '예: OO대학교 컴퓨터공학과' },
  { key: 'github', label: 'GitHub', max: 200, placeholder: '예: github.com/seoyeon-dev' }
];

// 이력서 기본 정보(이름·학력·GitHub) 입력칸. 공통·특화 이력서 편집이 함께 쓴다.
export default function BasicInfoFields({ basic = {}, onChange }) {
  return (
    <div className="resume-fields">
      {BASIC_FIELDS.map((f) => (
        <TextField key={f.key} id={`basic-${f.key}`} label={f.label} max={f.max} placeholder={f.placeholder} value={basic[f.key]} onChange={(v) => onChange({ ...basic, [f.key]: v })} />
      ))}
    </div>
  );
}
