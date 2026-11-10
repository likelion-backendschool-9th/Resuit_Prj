import { Suspense } from 'react';
import HashtagSearchPage from '@/views/HashtagSearchPage.jsx';

export const metadata = {
  title: '해시태그 검색 · Resuit',
  description: '해시태그로 게시글을 검색합니다.',
};

export default function SearchRoute() {
  return (
    <Suspense fallback={<div className="empty" role="status">검색을 준비하는 중…</div>}>
      <HashtagSearchPage />
    </Suspense>
  );
}
