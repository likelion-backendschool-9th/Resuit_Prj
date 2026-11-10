import { useState } from 'react';
import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import ResumePreview from './ResumePreview.jsx';
import FeedbackComments from './FeedbackComments.jsx';
import { FIELD_LABEL, SECTION_LABEL, avatarColor, profileHref, timeText } from './labels.js';
import './FeedbackRequestCard.css';

// 피드백 게시판의 이력서 카드. 펼치면 섹션별 피드백 목록과 작성란이 보인다.
export default function FeedbackRequestCard({ request }) {
  const [open, setOpen] = useState(false);
  // 4.8 한 번 펼친 뒤에는 접어도 댓글과 작성 중인 글을 그대로 둔다
  const [opened, setOpened] = useState(false);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [count, setCount] = useState(request.commentCount);
  const [adopted, setAdopted] = useState(request.adopted);
  const { author } = request;
  const name = author.nickname || author.username;
  const resumeType = request.resumeType === 'TARGET'
    ? ['특화 이력서', request.companyName, request.jobTitle].filter(Boolean).join(' · ')
    : '공통 이력서';

  const toggle = () => { setOpen((v) => !v); setOpened(true); };

  return (
    <article className="fb-card">
      {/* 프로필 사진·이름·아이디를 누르면 요청한 사람의 프로필(/아이디)로 이동한다 */}
      <Link href={profileHref(author.username)} className="fb-profile__avatar" aria-label={`${name} 프로필 보기`}>
        <Avatar initial={name.charAt(0)} color={avatarColor(author.username)} src={author.profileImageUrl} />
      </Link>
      <div className="fb-card__body">
        <header className="row-wrap" style={{ gap: 6 }}>
          <Link href={profileHref(author.username)} className="fb-profile__name">{name}</Link>
          <span className="muted"><Link href={profileHref(author.username)} className="fb-profile__handle">@{author.username}</Link> · {timeText(request.createdAt)}</span>
          {request.category && <Badge tone="general">{FIELD_LABEL[request.category] ?? request.category}</Badge>}
          {request.mine && <Badge tone="accent">내 요청</Badge>}
          {adopted && <Badge tone="accepted">채택 완료</Badge>}
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

        <Button variant="accent-outline" className="fb-card__toggle" aria-expanded={open} onClick={toggle}>
          <Icon name="msg" size={16} />
          {open ? '피드백 접기' : count ? `피드백 ${count}개 보기 · 남기기` : '첫 피드백 남기기'}
        </Button>

        {opened && (
          <div hidden={!open}>
            <FeedbackComments request={request} onAdded={() => setCount((c) => c + 1)} onDeleted={() => setCount((c) => Math.max(0, c - 1))} onAdopted={() => setAdopted(true)} />
          </div>
        )}
      </div>
    </article>
  );
}
