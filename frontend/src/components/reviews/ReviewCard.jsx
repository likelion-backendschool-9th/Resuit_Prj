import Avatar from '@/components/common/Avatar.jsx';
import Button from '@/components/common/Button.jsx';
import './ReviewCard.css';

export default function ReviewCard({ review, period, pending, onLike, onAccept }) {
  return (
    <article className="review-card">
      <div className="review-card__head">
        <Avatar initial={(review.nickname || review.username).slice(0, 1)} />
        <div><strong>{review.nickname || review.username}</strong>
          <div className="review-card__meta">@{review.username} · {review.periodStart} 시작{period ? ` · ${period === 'WEEKLY' ? '주간' : '월간'}` : ''}</div>
        </div>
        <span className="review-card__rank">{review.rank}위</span>
      </div>
      {review.section && <p className="review-card__meta">{review.section} 피드백</p>}
      <p className="review-card__content">{review.content}</p>
      <div className="review-card__actions">
        <Button variant={review.liked ? 'primary' : 'outline'} size="sm" aria-pressed={review.liked}
          disabled={pending !== null} onClick={() => onLike(review)}>
          {pending === review.reviewId ? '처리 중' : `좋아요 ${review.likeCount}`}
        </Button>
        {review.canAccept && onAccept && <Button variant="accent-outline" size="sm"
          disabled={pending !== null} onClick={() => onAccept(review)}>채택하기</Button>}
        {review.adopted && <span className="review-card__meta">요청자가 채택한 리뷰</span>}
      </div>
    </article>
  );
}
