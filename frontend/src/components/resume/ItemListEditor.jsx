'use client';

import Button, { IconButton } from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import './ResumeFields.css';

const UP = { transform: 'rotate(180deg)' };

// 프로젝트·수상처럼 여러 개를 쓰는 항목의 편집 목록. 추가·삭제·순서 바꾸기를 맡고,
// 항목 하나의 입력칸은 renderItem(item, update, index)로 받는다.
export default function ItemListEditor({ items, onChange, renderItem, createItem, itemLabel, max, emptyText }) {
  const update = (index, patch) => onChange(items.map((item, i) => (i === index ? { ...item, ...patch } : item)));
  const remove = (index) => onChange(items.filter((_, i) => i !== index));
  const move = (index, step) => {
    const next = [...items];
    [next[index], next[index + step]] = [next[index + step], next[index]];
    onChange(next);
  };

  return (
    <div className="stack" style={{ gap: 10 }}>
      {items.length === 0 && <span className="small muted">{emptyText}</span>}
      {items.map((item, i) => (
        <div key={item.id ?? i} className="resume-item">
          <div className="resume-item__head">
            <span className="xs bold muted">{itemLabel} {i + 1}</span>
            <div className="row" style={{ gap: 2 }}>
              <IconButton label={`${itemLabel} ${i + 1} 위로`} className="icon-btn--plain" onClick={() => move(i, -1)} disabled={i === 0}><Icon name="chevronDown" size={18} style={UP} /></IconButton>
              <IconButton label={`${itemLabel} ${i + 1} 아래로`} className="icon-btn--plain" onClick={() => move(i, 1)} disabled={i === items.length - 1}><Icon name="chevronDown" size={18} /></IconButton>
              <IconButton label={`${itemLabel} ${i + 1} 삭제`} className="icon-btn--plain" onClick={() => remove(i)}><Icon name="trash" size={18} /></IconButton>
            </div>
          </div>
          {renderItem(item, (patch) => update(i, patch), i)}
        </div>
      ))}
      <div className="row" style={{ gap: 10 }}>
        <Button variant="accent-outline" onClick={() => onChange([...items, createItem()])} disabled={items.length >= max}>
          <Icon name="plus" size={16} />{itemLabel} 추가
        </Button>
        <span className="xs muted">{items.length} / {max}개</span>
      </div>
    </div>
  );
}
