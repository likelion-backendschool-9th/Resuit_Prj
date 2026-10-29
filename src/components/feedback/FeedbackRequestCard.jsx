import { useState } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import { ChipRadioGroup } from '../common/Chip.jsx';
import './FeedbackRequestCard.css';

const SECTIONS = ['전체', '학력', '기술 스택', '프로젝트', '자기소개'].map((s) => ({ id: s, label: s }));

// 피드백 게시판의 이력서 카드. 펼치면 섹션별 피드백 목록과 작성란이 보인다.
export default function FeedbackRequestCard({ request, defaultOpen = false }) {
  const { addFeedback } = useAppState();
  const [open, setOpen] = useState(defaultOpen);
  const [section, setSection] = useState('전체');
  const [draft, setDraft] = useState('');
  const count = request.feedbacks.length;
  const inputId = `feedback-${request.id}`;

  const submit = () => {
    if (!draft.trim()) return;
    addFeedback(request.id, section, draft.trim());
    setDraft('');
  };

  return (
    <article className="fb-card">
      <Avatar initial={request.initial} color={request.av} />
      <div className="fb-card__body">
        <header className="row-wrap" style={{ gap: 6 }}>
          <strong>{request.author}</strong>
          <span className="muted">@{request.handle} · {request.time}</span>
          <Badge tone="general">{request.field}</Badge>
          {request.mine && <Badge tone="accent">내 요청</Badge>}
        </header>

        <div className="fb-card__resume">
          <div className="resume-thumb" aria-hidden="true"><span /><span /><span /><span /></div>
          <div className="grow">
            <div className="xs muted">{request.resumeType}</div>
            <div className="bold">{request.resumeTitle}</div>
            <div className="small muted">{request.sections}</div>
          </div>
        </div>

        <p>{request.message}</p>
        <div className="row-wrap" style={{ gap: 6 }}>
          <span className="xs bold muted">봐 줬으면 하는 부분</span>
          {request.focus.map((f) => <Badge key={f} tone="special">{f}</Badge>)}
        </div>

        <Button variant="accent-outline" className="fb-card__toggle" aria-expanded={open} onClick={() => setOpen((v) => !v)}>
          <Icon name="msg" size={16} />
          {open ? '피드백 접기' : count ? `피드백 ${count}개 보기 · 남기기` : '첫 피드백 남기기'}
        </Button>

        {open && (
          <div className="fb-card__panel">
            {request.feedbacks.map((f) => (
              <div key={f.id} className="fb-item">
                <Avatar initial={f.initial} color={f.av} size={32} company={f.company} />
                <div className="grow">
                  <div className="row-wrap" style={{ gap: 6, fontSize: 14 }}>
                    <strong>{f.author}</strong>
                    <Badge tone="accent">{f.section}</Badge>
                    <span className="xs muted">{f.time}</span>
                  </div>
                  <p style={{ fontSize: 14 }}>{f.text}</p>
                </div>
              </div>
            ))}

            <div className="stack" style={{ gap: 8 }}>
              <ChipRadioGroup options={SECTIONS} value={section} onChange={setSection} label="피드백할 섹션" size="sm" />
              <label htmlFor={inputId} className="sr-only">피드백 작성</label>
              <textarea
                id={inputId}
                className="textarea"
                rows={3}
                placeholder="좋은 점 하나, 고칠 점 하나를 구체적으로 남겨 주세요."
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
              />
              <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Button onClick={submit} disabled={!draft.trim()}>피드백 등록</Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </article>
  );
}
