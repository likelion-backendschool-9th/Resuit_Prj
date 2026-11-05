'use client';

import Link from 'next/link';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Icon from '../components/common/Icon.jsx';

// 공고 API 연동 전까지 상세는 비어 있다.
export default function JobDetailPage({ id }) {
  void id;
  return (
    <>
      <MainColumn>
        <div className="page-head">
          <Link href="/jobs" className="icon-btn icon-btn--plain" aria-label="공고 목록으로"><Icon name="back" /></Link>
          <h1>공고</h1>
        </div>
        <div className="empty">공고를 찾을 수 없어요.</div>
      </MainColumn>
      <RightAside showReviews label="베스트 리뷰 및 명예의 전당" />
    </>
  );
}
