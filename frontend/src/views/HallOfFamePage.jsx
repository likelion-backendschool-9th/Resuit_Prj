'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import ReviewCard from '@/components/reviews/ReviewCard.jsx';
import { getHallOfFame, setReviewLiked, acceptReview } from '@/services/reviews.js';
import './BestReviewsPage.css';

export default function HallOfFamePage() {
  const [page, setPage] = useState(0);
  const [result, setResult] = useState({ page: -1, reviews: [], hasNext: false });
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
    getHallOfFame(page, controller.signal).then(setResult)
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [page, retry]);

  async function toggleLike(review) {
    setPending(review.reviewId);
    setNotice('');
    try {
      const updated = await setReviewLiked(review.reviewId, !review.liked);
      setResult((current) => ({ ...current, reviews: current.reviews.map((entry) => {
        if (entry.review.reviewId !== updated.reviewId) return entry;
        const item = entry.review;
        return { ...entry, review: { ...item, liked: updated.liked,
          likeCount: item.likeCount + (item.liked === updated.liked ? 0 : updated.liked ? 1 : -1) } };
      }) }));
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

  const entries = result.page === page ? result.reviews : [];
  return (
    <>
      <MainColumn>
        <div className="page-title"><h1>명예의 전당</h1><Link href="/reviews/best" className="text-link">베스트 리뷰</Link></div>
        <p className="page-desc">역대 주간·월간 베스트 리뷰의 선정 기록이에요.</p>
        <p className="reviews__notice" role="status">{notice}</p>
        {loading ? <div className="empty" role="status">선정 기록을 불러오는 중이에요.</div>
          : error ? <div className="empty" role="alert"><p>{error}</p><Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>
          : entries.length === 0 ? <div className="empty">아직 이 페이지에 선정 기록이 없어요.</div>
          : entries.map((entry) => <ReviewCard key={entry.selectionId} review={entry.review}
            period={entry.period} pending={pending} onLike={toggleLike} onAccept={handleAccept} />)}
        <nav className="reviews__pagination" aria-label="선정 기록 페이지">
          <Button variant="outline" disabled={page === 0 || loading || pending !== null} onClick={() => setPage((v) => v - 1)}>이전</Button>
          <span aria-live="polite">{page + 1} 페이지</span>
          <Button variant="outline" disabled={loading || !!error || result.page !== page || !result.hasNext || pending !== null}
            onClick={() => setPage((v) => v + 1)}>다음</Button>
        </nav>
      </MainColumn>
      <RightAside label="명예의 전당 안내"><section className="card"><h2>기록으로 남는 피드백</h2>
        <p className="text-2">최근 선정 기간부터 보여드려요. 같은 리뷰가 여러 기간에 선정되면 각각의 기록으로 남아요.</p>
      </section></RightAside>
    </>
  );
}
