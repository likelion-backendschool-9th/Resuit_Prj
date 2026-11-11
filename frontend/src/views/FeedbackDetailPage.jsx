'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Avatar from '@/components/common/Avatar.jsx';
import Badge from '@/components/common/Badge.jsx';
import Button from '@/components/common/Button.jsx';
import Icon from '@/components/common/Icon.jsx';
import FeedbackComments from '@/components/feedback/FeedbackComments.jsx';
import ResumePreview from '@/components/feedback/ResumePreview.jsx';
import { FIELD_LABEL, SECTION_LABEL, avatarColor, timeText } from '@/components/feedback/labels.js';
import { getFeedbackRequest } from '@/services/feedbackRequests.js';
import '@/components/feedback/FeedbackRequestCard.css';

// 피드백 게시글 상세. 이력서 미리보기와 댓글 작성을 한 화면에서 처리한다.
export default function FeedbackDetailPage({ id }) {
  const [request, setRequest] = useState(null);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  const [previewOpen, setPreviewOpen] = useState(true);
  const [count, setCount] = useState(0);
  const [adopted, setAdopted] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    setRequest(null);
    setError('');
    getFeedbackRequest(id, controller.signal)
      .then((data) => {
        setRequest(data);
        setCount(data.commentCount);
        setAdopted(data.adopted);
      })
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); });
    return () => controller.abort();
  }, [id, retry]);

  const title = (
    <div className="page-title">
      <h1>피드백 요청</h1>
      <Button to="/feedback" variant="outline" size="sm">목록으로</Button>
    </div>
  );

  if (error) {
    return (
      <>
        <MainColumn>
          {title}
          <div className="empty" role="alert">
            <p>{error}</p>
            <Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button>
          </div>
        </MainColumn>
        <RightAside label="리뷰 추천 및 보조 정보" />
      </>
    );
  }

  if (!request) {
    return (
      <>
        <MainColumn>{title}<div className="empty" role="status">불러오는 중이에요.</div></MainColumn>
        <RightAside label="리뷰 추천 및 보조 정보" />
      </>
    );
  }

  const { author } = request;
  const name = author.nickname || author.username;
  const resumeType = request.resumeType === 'TARGET'
    ? ['특화 이력서', request.companyName, request.jobTitle].filter(Boolean).join(' · ')
    : '공통 이력서';

  return (
    <>
      <MainColumn>
        {title}
        <article className="fb-card fb-card--detail">
          <Avatar initial={name.charAt(0)} color={avatarColor(author.username)} src={author.profileImageUrl} />
          <div className="fb-card__body">
            <header className="row-wrap" style={{ gap: 6 }}>
              <strong>{name}</strong>
              {author.displayedBadgeName && <Badge tone="sharer">{author.displayedBadgeName}</Badge>}
              <span className="muted">@{author.username} · {timeText(request.createdAt)}</span>
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
              <span className="fb-card__preview-label small">
                <Icon name="eye" size={16} />{previewOpen ? '미리보기 닫기' : '이력서 보기'}
              </span>
            </button>
            {previewOpen && (
              <ResumePreview type={request.resumeType} id={request.resumeId} requested={request.sections} />
            )}

            {request.content && <p>{request.content}</p>}
            {request.sections.length > 0 && (
              <div className="row-wrap" style={{ gap: 6 }}>
                <span className="xs bold muted">봐 줬으면 하는 부분</span>
                {request.sections.map((s) => (
                  <Badge key={s} tone="focus" size="md">{SECTION_LABEL[s] ?? s}</Badge>
                ))}
              </div>
            )}

            <p className="small muted">
              <Icon name="msg" size={16} /> 피드백 {count}개
            </p>

            <FeedbackComments
              request={{ ...request, adopted }}
              onAdded={() => setCount((c) => c + 1)}
              onDeleted={() => setCount((c) => Math.max(0, c - 1))}
              onAdopted={() => setAdopted(true)}
            />
          </div>
        </article>
      </MainColumn>

      <RightAside showReviews label="리뷰 추천 및 보조 정보">
        <section className="card">
          <h2>좋은 피드백 쓰는 법</h2>
          <ol className="text-2" style={{ paddingLeft: 18, fontSize: 14, lineHeight: 1.8 }}>
            <li>섹션을 골라 어디에 대한 의견인지 밝히기</li>
            <li>좋은 점 하나, 고칠 점 하나</li>
            <li>고친 예시 문장을 함께 적기</li>
          </ol>
          <p className="xs muted" style={{ marginTop: 8 }}>
            <Link href="/feedback" className="text-link">다른 요청 보기</Link>
          </p>
        </section>
      </RightAside>
    </>
  );
}
