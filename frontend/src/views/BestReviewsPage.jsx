'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Tabs from '@/components/common/Tabs.jsx';
import Button from '@/components/common/Button.jsx';
import ReviewCard from '@/components/reviews/ReviewCard.jsx';
import Link from 'next/link';
import { getBestReviews, setReviewLiked, acceptReview } from '@/services/reviews.js';
import './BestReviewsPage.css';

const TABS = [{ id: 'WEEKLY', label: '주간' }, { id: 'MONTHLY', label: '월간' }];

export default function BestReviewsPage() {
  const [period, setPeriod] = useState('WEEKLY');
  const [result, setResult] = useState({ period: null, reviews: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [pending, setPending] = useState(null);
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    setNotice('');
    getBestReviews(period, controller.signal)
      .then(setResult)
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [period, retry]);

  async function toggleLike(review) {
    setPending(review.reviewId);
    setNotice('');
    try {
      const updated = await setReviewLiked(review.reviewId, !review.liked);
      setResult((current) => ({ ...current, reviews: current.reviews.map((item) => item.reviewId === updated.reviewId
        ? { ...item, liked: updated.liked, likeCount: item.likeCount + (updated.liked === item.liked ? 0 : updated.liked ? 1 : -1) }
        : item) }));
    } catch (err) {
      setNotice(err.message);
    } finally {
      setPending(null);
    }
  }

  async function handleAccept(review) {
    setPending(review.reviewId);
    setNotice('');
    try {
      await acceptReview(review.reviewId);
      setRetry((value) => value + 1);
      setNotice('리뷰를 채택했어요.');
    } catch (err) {
      setNotice(err.message);
    } finally {
      setPending(null);
    }
  }

  const reviews = result.period === period ? result.reviews : [];
  return (
    <>
      <MainColumn>
        <div className="page-title"><h1>베스트 리뷰</h1><Link href="/reviews/hall-of-fame" className="text-link">명예의 전당</Link></div>
        <p className="page-desc">이력서를 더 나아지게 만든 피드백을 만나 보세요.</p>
        <Tabs items={TABS} value={period} onChange={setPeriod} label="선정 기간" />
        <p className="reviews__notice" role="status">{notice}</p>
        {loading ? <div className="empty" role="status">리뷰를 불러오는 중이에요.</div>
          : error ? <div className="empty" role="alert"><p>{error}</p><Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>
          : reviews.length === 0 ? <div className="empty">아직 선정된 {period === 'WEEKLY' ? '주간' : '월간'} 리뷰가 없어요.</div>
          : reviews.map((review) => (
            <ReviewCard key={review.reviewId} review={review} pending={pending} onLike={toggleLike} onAccept={handleAccept} />
          ))}
      </MainColumn>
      <RightAside label="베스트 리뷰 안내">
        <section className="card"><h2>함께 성장하는 피드백</h2><p className="text-2">도움이 된 리뷰에 좋아요를 남겨 주세요. 주간·월간별 가장 최근 선정 결과를 보여드려요.</p></section>
      </RightAside>
    </>
  );
}
