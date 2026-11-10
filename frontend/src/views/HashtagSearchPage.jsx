'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import SearchBox from '../components/common/SearchBox.jsx';
import PostCard from '../components/feed/PostCard.jsx';
import Button from '../components/common/Button.jsx';
import { getFeed } from '../services/posts.js';

export default function HashtagSearchPage() {
  const params = useSearchParams();
  const tag = (params.get('hashtag') || '').replace(/^#+/, '').trim();
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!tag) {
      setPosts([]);
      setLast(true);
      setError('');
      return undefined;
    }
    const controller = new AbortController();
    setLoading(true);
    setError('');
    getFeed({ tab: 'hashtag', tag, page: 0, signal: controller.signal })
      .then((data) => {
        setPosts(data.posts);
        setPage(data.page);
        setLast(data.last);
      })
      .catch((err) => {
        if (err.name === 'AbortError') return;
        setPosts([]);
        setError(err.message || '검색에 실패했어요.');
      })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [tag]);

  async function loadMore() {
    if (loading || last || !tag) return;
    setLoading(true);
    setError('');
    try {
      const data = await getFeed({ tab: 'hashtag', tag, page: page + 1 });
      setPosts((prev) => [...prev, ...data.posts]);
      setPage(data.page);
      setLast(data.last);
    } catch (err) {
      setError(err.message || '더 불러오지 못했어요.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <MainColumn>
        <div className="page-access" style={{ paddingBottom: 8 }}>
          <h1 id="hashtag-search-title">{tag ? `#${tag}` : '해시태그 검색'}</h1>
          <p className="muted">
            {tag
              ? '이 해시태그가 달린 게시글이에요.'
              : '오른쪽 검색창에 #태그 형식으로 검색해 보세요.'}
          </p>
        </div>
        {!tag && <div className="empty">검색어를 입력해 주세요.</div>}
        {tag && posts.map((post) => (
          <PostCard key={post.id} post={post} onUpdated={(next) => setPosts((list) => list.map((item) => (item.id === next.id ? next : item)))} onDeleted={(id) => setPosts((list) => list.filter((item) => item.id !== id))} />
        ))}
        {tag && !loading && !error && !posts.length && <div className="empty">이 해시태그의 게시글이 아직 없어요.</div>}
        <div className="empty" aria-live="polite" aria-busy={loading}>
          {loading && <p role="status">게시글을 불러오는 중…</p>}
          {error && <p role="alert">{error}</p>}
          {tag && ( !last || error) && (
            <Button variant="outline" onClick={loadMore} disabled={loading}>
              {loading ? '불러오는 중…' : error ? '다시 시도' : '게시글 더 보기'}
            </Button>
          )}
        </div>
      </MainColumn>
      <RightAside showReviews label="검색 및 리뷰 추천">
        <SearchBox />
      </RightAside>
    </>
  );
}
