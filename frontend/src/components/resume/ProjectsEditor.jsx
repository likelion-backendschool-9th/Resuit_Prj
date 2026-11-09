'use client';

import Icon from '../common/Icon.jsx';
import ItemListEditor from './ItemListEditor.jsx';
import { TextField, TextAreaField } from './ResumeFields.jsx';
import { MAX_PROJECTS, newProject } from './resumeContent.js';

// 프로젝트 목록 편집. selectable이면(특화 이력서) 프로젝트마다 이 회사에 제출할지 고른다(selected).
// 근거(레포·블로그)는 보여 주기만 하고 순서를 바꿔도 해당 프로젝트에 그대로 붙어 있다.
export default function ProjectsEditor({ projects, onChange, selectable = false }) {
  return (
    <ItemListEditor
      items={projects} onChange={onChange}
      createItem={newProject} itemLabel="프로젝트" max={MAX_PROJECTS} emptyText="아직 작성한 프로젝트가 없어요"
      renderItem={(p, update, i) => (
        <>
          {selectable && (
            <label className="check" style={{ minHeight: 0, gap: 8, fontSize: 14 }}>
              <input type="checkbox" checked={p.selected !== false} onChange={(e) => update({ selected: e.target.checked })} />
              이 회사에 제출
            </label>
          )}
          <TextField id={`project-${i}-title`} label="프로젝트 이름" max={100} placeholder="예: 스터디 매칭 웹앱" value={p.title} onChange={(v) => update({ title: v })} />
          <TextAreaField id={`project-${i}-desc`} label="프로젝트 설명" max={1000} rows={3} placeholder="사용한 기술, 팀 규모, 맡은 일과 결과를 적어 주세요." value={p.desc} onChange={(v) => update({ desc: v })} />
          {p.evidence?.length > 0 && (
            <div className="row-wrap" style={{ gap: 6 }}>
              <span className="xs bold muted">근거</span>
              {p.evidence.map((ev) => {
                const type = String(ev.type).toLowerCase();
                return <span key={ev.label} className={`evidence-chip evidence-chip--${type}`}><Icon name={type === 'repo' ? 'repo' : 'rss'} size={14} />{ev.label}</span>;
              })}
            </div>
          )}
        </>
      )}
    />
  );
}
