'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import { StarIcon } from '../common/Icon.jsx';
import { getReviewHighlights } from '@/services/reviews.js';
import './ReviewHighlights.css';

function HighlightSection({ title, href, description, state, historic }) {
  return (
    <section className={`review-highlights ${historic ? 'review-highlights--history' : ''}`}>
      <header className="review-highlights__head">
        <h2><StarIcon size={14} />{title}</h2>
        <Link href={href} className="review-highlights__more" aria-label={`${title} 전체 보기`}>전체 보기 →</Link>
      </header>
      <p className="review-highlights__description">{description}</p>
      {state.loading ? <p className="review-highlights__status" role="status">목록을 불러오는 중이에요.</p>
        : state.error ? <p className="review-highlights__status">리뷰를 불러오지 못했어요. 전체 보기에서 다시 확인해 주세요.</p>
        : state.items.length === 0 ? <p className="review-highlights__status">{historic ? '아직 선정된 리뷰어가 없어요.' : '아직 선정된 리뷰가 없어요.'}</p>
        : <ol className="review-highlights__list">{state.items.map(({ key, review, reviewer }) => (
          <li key={key}>
            {historic ? <Link href={`/${encodeURIComponent(reviewer.username)}`} className="review-highlights__item">
              <span className="review-highlights__rank">{reviewer.rank}<small>위</small></span>
              <div className="review-highlights__body">
                <div className="review-highlights__author"><Avatar size={24} initial={(reviewer.nickname || reviewer.username).slice(0, 1)} color="av7" /><strong>{reviewer.nickname || reviewer.username}</strong></div>
                <p className="review-highlights__excerpt">{reviewer.representativeContent}</p>
                <span className="review-highlights__meta">선정 리뷰 {reviewer.reviewCount}개 · 누적 {reviewer.selectionCount}회</span>
              </div>
            </Link> : <Link href={href} className="review-highlights__item" aria-label={`${review.nickname || review.username}님의 ${review.rank}위 리뷰 보기`}>
              <span className="review-highlights__rank">{review.rank}<small>위</small></span>
              <div className="review-highlights__body">
                <div className="review-highlights__author"><Avatar size={24} initial={(review.nickname || review.username).slice(0, 1)} color="av4" />
                  <strong>{review.nickname || review.username}</strong></div>
                <p className="review-highlights__excerpt">{review.content}</p>
                <span className="review-highlights__meta">좋아요 {review.likeCount}</span>
              </div>
            </Link>}
          </li>
        ))}</ol>}
    </section>
  );
}

export default function ReviewHighlights() {
  const [best, setBest] = useState({ loading: true, items: [] });
  const [history, setHistory] = useState({ loading: true, items: [] });
  useEffect(() => {
    const controller = new AbortController();
    getReviewHighlights(controller.signal)
      .then((data) => {
        if (controller.signal.aborted) return;
        setBest({ loading: false, items: data.reviews.map((review) => ({ key: review.rank, review })) });
        setHistory({ loading: false, items: data.reviewers.map((reviewer) => ({ key: reviewer.username, reviewer })) });
      })
      .catch(() => {
        if (controller.signal.aborted) return;
        setBest({ loading: false, items: [], error: true });
        setHistory({ loading: false, items: [], error: true });
      });
    return () => controller.abort();
  }, []);
  return <>
    <HighlightSection title="명예의 전당" href="/reviews/hall-of-fame" description="좋은 리뷰를 꾸준히 남긴 리뷰어" state={history} historic />
    <HighlightSection title="베스트 리뷰" href="/reviews/best" description="최근 주간 선정 피드백" state={best} />
  </>;
}
