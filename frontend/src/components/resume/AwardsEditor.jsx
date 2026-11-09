'use client';

import ItemListEditor from './ItemListEditor.jsx';
import { TextField } from './ResumeFields.jsx';
import { MAX_AWARDS, newAward } from './resumeContent.js';

// 수상 목록 편집. 수상은 { title, date } 모양이다.
export default function AwardsEditor({ awards, onChange }) {
  return (
    <ItemListEditor
      items={awards} onChange={onChange}
      createItem={newAward} itemLabel="수상" max={MAX_AWARDS} emptyText="아직 작성한 수상 이력이 없어요"
      renderItem={(a, update, i) => (
        <div className="resume-fields">
          <TextField id={`award-${i}-title`} label="수상 이름" max={100} placeholder="예: 교내 해커톤 우수상" value={a.title} onChange={(v) => update({ title: v })} />
          <TextField id={`award-${i}-date`} label="받은 날" max={20} placeholder="예: 2025-11" value={a.date} onChange={(v) => update({ date: v })} />
        </div>
      )}
    />
  );
}
