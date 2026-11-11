'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import Avatar from '@/components/common/Avatar.jsx';
import Icon, { StarIcon } from '@/components/common/Icon.jsx';
import { getHallOfFame } from '@/services/reviews.js';
import './BestReviewsPage.css';
import './HallOfFamePage.css';

export default function HallOfFamePage() {
  const [page, setPage] = useState(0);
  const [result, setResult] = useState({ page: -1, reviewers: [], hasNext: false });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    getHallOfFame(page, controller.signal).then((data) => { if (!controller.signal.aborted) setResult(data); })
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [page, retry]);
  const reviewers = result.page === page ? result.reviewers : [];
  return <>
    <MainColumn>
      <div className="page-title"><h1>명예의 전당</h1><Link href="/reviews/best" className="text-link">베스트 리뷰</Link></div>
      <div className="hall__intro">
        <div className="hall__intro-copy">
        <span className="hall__eyebrow">좋은 피드백을 쌓아 온 사람들</span>
        <h2>좋은 피드백은<br />사람으로 기억됩니다.</h2>
        <p>베스트에 선정된 리뷰를 많이 쓴 리뷰어를 소개해요.</p>
        <div className="hall__legend"><span>누적 선정 리뷰 수 기준</span></div>
        </div>
        <div className="hall__seal" aria-hidden="true"><StarIcon size={38} /><span>HALL OF</span><strong>FAME</strong></div>
      </div>
      {loading ? <div className="empty" role="status">리뷰어를 불러오는 중이에요.</div>
        : error ? <div className="empty" role="alert"><p>{error}</p><Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>
        : reviewers.length === 0 ? <div className="empty">아직 선정된 리뷰어가 없어요.</div>
        : <><div className="hall__list-heading"><h2>리뷰어 랭킹</h2><span>총 {result.totalElements}명 · 누적 실적</span></div><div className="hall__groups hall__ranking">{reviewers.map((reviewer) => (
          <div className="hall__card-stage" key={reviewer.username}>
            <article className={`hall__entry hall__entry--${reviewer.rank}`}>
              <div className="hall__corner">
                <i className="hall__corner-curve hall__corner-curve--left" aria-hidden="true" />
                <i className="hall__corner-curve hall__corner-curve--bottom" aria-hidden="true" />
                <Link className="hall__profile-action" href={`/${encodeURIComponent(reviewer.username)}`}
                  aria-label={`${reviewer.nickname || reviewer.username}님의 프로필 보기`} title="프로필 보기">
                  <Icon name="user" size={24} />
                </Link>
              </div>
              <header className="hall__reviewer-header">
                <div className="hall__rank-label">{String(reviewer.rank).padStart(2, '0')}위
                  {reviewer.rank === 1 && <span><StarIcon size={12} /> 최상위 리뷰어</span>}
                </div>
                <div className="hall__identity">
                  <Avatar initial={(reviewer.nickname || reviewer.username).slice(0, 1)} color={reviewer.rank === 1 ? 'av4' : reviewer.rank === 2 ? 'av5' : 'av3'} size={46} />
                  <div><h2>{reviewer.nickname || reviewer.username}</h2><p>@{reviewer.username}</p></div>
                </div>
              </header>
              <details className="hall__representative" open={reviewer.rank === 1}>
                <summary>대표 리뷰 읽기 <span aria-hidden="true">⌄</span></summary>
                <p>{reviewer.representativeContent}</p>
              </details>
              <footer className="hall__card-footer">
                <dl className="hall__stats">
                  <div><dt>선정된 리뷰</dt><dd>{reviewer.reviewCount}<small>개</small></dd></div>
                  <div><dt>누적 선정</dt><dd>{reviewer.selectionCount}<small>회</small></dd></div>
                  <div><dt>주간 / 월간</dt><dd>{reviewer.weeklyCount}<small> / </small>{reviewer.monthlyCount}<small>회</small></dd></div>
                </dl>
                <p className="hall__last-selection">최근 선정 · {reviewer.latestSelection}</p>
              </footer>
            </article>
          </div>
        ))}</div></>}
      <nav className="reviews__pagination" aria-label="리뷰어 순위 페이지">
        <Button variant="outline" disabled={page === 0 || loading} onClick={() => setPage((v) => v - 1)}>이전</Button>
        <span aria-live="polite">{page + 1} 페이지</span>
        <Button variant="outline" disabled={loading || !!error || result.page !== page || !result.hasNext} onClick={() => setPage((v) => v + 1)}>다음</Button>
      </nav>
    </MainColumn>
    <RightAside label="명예의 전당 집계 안내"><section className="card"><h2>리뷰보다, 리뷰어를 기억해요</h2>
      <p className="text-2">같은 리뷰가 여러 번 선정돼도 ‘선정된 리뷰’는 한 개로 세어요. 누적 선정 횟수는 각각 더해요.</p>
      <p className="hall__aside-note">선정 리뷰 수가 같으면 누적 선정 횟수, 그다음 사용자 이름 순으로 표시해요.</p>
      <p className="hall__aside-note">대표 리뷰는 가장 높은 순위에 오른 글이에요. 같은 순위라면 최근 선정된 글을 보여줘요.</p>
    </section></RightAside>
  </>;
}
