import { useState } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import { ChipRadioGroup } from '../common/Chip.jsx';
import ResumePreview from './ResumePreview.jsx';
import { FIELD_LABEL, SECTION_LABEL } from './labels.js';
import './FeedbackRequestCard.css';

const SECTIONS = ['전체', '학력', '기술 스택', '프로젝트', '자기소개'].map((s) => ({ id: s, label: s }));
const AVATAR_COLORS = ['av1', 'av2', 'av3', 'av4', 'av5', 'av6'];

// 같은 사람은 항상 같은 아바타 색이 나오게 username으로 고른다
function avatarColor(username = '') {
  let sum = 0;
  for (const ch of username) sum += ch.charCodeAt(0);
  return AVATAR_COLORS[sum % AVATAR_COLORS.length];
}

function timeText(value) {
  const minutes = Math.floor((Date.now() - new Date(value).getTime()) / 60000);
  if (!(minutes >= 1)) return '방금';
  if (minutes < 60) return `${minutes}분 전`;
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)}시간 전`;
  return `${Math.floor(minutes / (60 * 24))}일 전`;
}

// 피드백 게시판의 이력서 카드. 펼치면 섹션별 피드백 목록과 작성란이 보인다.
export default function FeedbackRequestCard({ request, defaultOpen = false }) {
  const { addFeedback, adoptFeedback } = useAppState();
  const [open, setOpen] = useState(defaultOpen);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [section, setSection] = useState('전체');
  const [draft, setDraft] = useState('');
  const { author } = request;
  const name = author.nickname || author.username;
  const feedbacks = request.feedbacks ?? [];
  const count = request.commentCount;
  const inputId = `feedback-${request.id}`;
  const resumeType = request.resumeType === 'TARGET'
    ? ['특화 이력서', request.companyName, request.jobTitle].filter(Boolean).join(' · ')
    : '공통 이력서';

  const submit = () => {
    if (!draft.trim()) return;
    addFeedback(request.id, section, draft.trim());
    setDraft('');
  };

  return (
    <article className="fb-card">
      <Avatar initial={name.charAt(0)} color={avatarColor(author.username)} src={author.profileImageUrl} />
      <div className="fb-card__body">
        <header className="row-wrap" style={{ gap: 6 }}>
          <strong>{name}</strong>
          <span className="muted">@{author.username} · {timeText(request.createdAt)}</span>
          {request.category && <Badge tone="general">{FIELD_LABEL[request.category] ?? request.category}</Badge>}
          {request.mine && <Badge tone="accent">내 요청</Badge>}
          {request.adopted && <Badge tone="accepted">채택 완료</Badge>}
        </header>

        <button type="button" className="fb-card__resume" aria-expanded={previewOpen} onClick={() => setPreviewOpen((v) => !v)}>
          <div className="resume-thumb" aria-hidden="true"><span /><span /><span /><span /></div>
          <div className="grow">
            <div className="xs muted">{resumeType}</div>
            <div className="bold">{request.resumeTitle}</div>
          </div>
          <span className="fb-card__preview-label small"><Icon name="eye" size={16} />{previewOpen ? '미리보기 닫기' : '이력서 보기'}</span>
        </button>
        {previewOpen && <ResumePreview type={request.resumeType} id={request.resumeId} requested={request.sections} />}

        {request.content && <p>{request.content}</p>}
        {request.sections.length > 0 && (
          <div className="row-wrap" style={{ gap: 6 }}>
            <span className="xs bold muted">봐 줬으면 하는 부분</span>
            {request.sections.map((s) => <Badge key={s} tone="focus" size="md">{SECTION_LABEL[s] ?? s}</Badge>)}
          </div>
        )}

        <Button variant="accent-outline" className="fb-card__toggle" aria-expanded={open} onClick={() => setOpen((v) => !v)}>
          <Icon name="msg" size={16} />
          {open ? '피드백 접기' : count ? `피드백 ${count}개 보기 · 남기기` : '첫 피드백 남기기'}
        </Button>

        {open && (
          <div className="fb-card__panel">
            {request.mine && <p className="small muted">가장 도움이 된 피드백 1개를 채택해 주세요. 채택은 리뷰어의 인증 배지 평가에 반영돼요.</p>}
            {feedbacks.map((f, i) => (
                <div key={f.id} className="fb-item">
                  <Avatar initial={f.initial} color={f.av} size={32} company={f.company} />
                  <div className="grow stack" style={{ gap: 4 }}>
                    <div className="row-wrap" style={{ gap: 6, fontSize: 14 }}>
                      <strong>{f.author}</strong>
                      {f.reviewerBadge && <Badge tone="verified"><Icon name="check" size={11} />{f.reviewerBadge}</Badge>}
                      <Badge tone="general">{f.section}</Badge>
                      <span className="xs muted">{f.time}</span>
                      {request.adopted === i && <span className="fb-item__adopt"><Badge tone="accepted" size="md">채택됨</Badge></span>}
                      {request.mine && request.adopted === undefined && f.author !== request.author && (
                        <Button variant="accent-outline" size="sm" className="fb-item__adopt" onClick={() => adoptFeedback(request.id, i)}>채택하기</Button>
                      )}
                    </div>
                    <p style={{ fontSize: 14 }}>{f.text}</p>
                  </div>
                </div>
            ))}

            <div className="stack" style={{ gap: 8, paddingTop: 4 }}>
              <ChipRadioGroup options={SECTIONS} value={section} onChange={setSection} label="피드백할 섹션" size="sm" />
              <label htmlFor={inputId} className="sr-only">피드백 작성</label>
              <textarea
                id={inputId}
                className="textarea fb-card__input"
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
