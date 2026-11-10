'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Icon from '../common/Icon.jsx';
import { FIELD_LABEL, SECTION_LABEL, avatarColor, profileHref, timeText } from './labels.js';
import './FeedbackRequestCard.css';

// 피드백 게시판 목록 카드. 게시글(PostCard)과 같이 본문 Link + 카드 클릭으로 상세 이동.
export default function FeedbackRequestCard({ request }) {
  const router = useRouter();
  const detailLink = `/feedback/${request.id}`;
  const { author } = request;
  const name = author.nickname || author.username;
  const resumeType = request.resumeType === 'TARGET'
    ? ['특화 이력서', request.companyName, request.jobTitle].filter(Boolean).join(' · ')
    : '공통 이력서';
  // 상대 시간은 클라이언트에서만 계산해 hydration 오류를 막는다.
  const [time, setTime] = useState('');
  useEffect(() => { setTime(timeText(request.createdAt)); }, [request.createdAt]);

  return (
    <article
      className="fb-card fb-card--linked"
      onClick={(event) => {
        if (event.defaultPrevented || event.target.closest('a, button, input, textarea, select, label, form')) return;
        if (window.getSelection()?.toString()) return;
        router.push(detailLink);
      }}
    >
      {/* 프로필 사진·이름·아이디를 누르면 요청한 사람의 프로필(/아이디)로 이동한다 */}
      <Link href={profileHref(author.username)} className="fb-profile__avatar" aria-label={`${name} 프로필 보기`}>
        <Avatar initial={name.charAt(0)} color={avatarColor(author.username)} src={author.profileImageUrl} />
      </Link>
      <div className="fb-card__body">
        <header className="row-wrap" style={{ gap: 6 }}>
          <Link href={profileHref(author.username)} className="fb-profile__name">{name}</Link>
          <span className="muted">
            <Link href={profileHref(author.username)} className="fb-profile__handle">@{author.username}</Link>
            {time ? ` · ${time}` : ''}
          </span>
          {request.category && <Badge tone="general">{FIELD_LABEL[request.category] ?? request.category}</Badge>}
          {request.mine && <Badge tone="accent">내 요청</Badge>}
          {request.adopted && <Badge tone="accepted">채택 완료</Badge>}
        </header>

        <Link href={detailLink} className="fb-card__detail-link">
          <div className="fb-card__resume fb-card__resume--static">
            <div className="resume-thumb" aria-hidden="true"><span /><span /><span /><span /></div>
            <div className="grow">
              <div className="xs muted">{resumeType}</div>
              <div className="bold">{request.resumeTitle}</div>
            </div>
          </div>
          {request.content && <p className="fb-card__snippet">{request.content}</p>}
        </Link>
        <Link href={detailLink} className="sr-only">{name}님의 피드백 요청 상세 보기</Link>

        {request.sections.length > 0 && (
          <div className="row-wrap" style={{ gap: 6 }}>
            <span className="xs bold muted">봐 줬으면 하는 부분</span>
            {request.sections.map((s) => <Badge key={s} tone="focus" size="md">{SECTION_LABEL[s] ?? s}</Badge>)}
          </div>
        )}

        <div className="fb-card__meta small muted">
          <Icon name="msg" size={16} />
          {request.commentCount ? `피드백 ${request.commentCount}개` : '피드백 남기기'}
        </div>
      </div>
    </article>
  );
}
