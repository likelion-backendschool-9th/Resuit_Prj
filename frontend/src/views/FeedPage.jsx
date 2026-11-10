'use client';

import { useEffect, useRef, useState } from 'react';
import { useAppState } from '../context/AppStateContext.jsx';
import { TECH_TAGS } from '../data/constants.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import { ChipRadioGroup } from '../components/common/Chip.jsx';
import SearchBox from '../components/common/SearchBox.jsx';
import Composer from '../components/feed/Composer.jsx';
import PostCard from '../components/feed/PostCard.jsx';
import Button from '../components/common/Button.jsx';
import useTimeline from '../components/feed/useTimeline.js';
import FeedTopButton from '../components/feed/FeedTopButton.jsx';
import { onFeedTopRequest } from '../components/feed/feedTop.js';

const TABS = [
  { id: 'recommend', label: '추천' },
  { id: 'following', label: '팔로잉' },
  { id: 'tech', label: '기술 정보' },
  { id: 'question', label: '질문' }
];
const COMPOSER_KIND = { recommend: 'general', tech: 'tech', question: 'question' };
const TECH_FILTERS = [{ id: 'all', label: '전체' }, ...TECH_TAGS.map((t) => ({ id: t, label: `#${t}` }))];

export default function FeedPage({ initialTimeline }) {
  const { me, loggedIn, authLoading, composedPost, clearComposedPost } = useAppState();
  const [tab, setTab] = useState('recommend');
  const [techFilter, setTechFilter] = useState('all');
  const { posts, hasMore, loading, error, loadMore, upsertPost, removePost, refresh } = useTimeline(initialTimeline, tab, loggedIn ? me.id : null, techFilter);
  const composerRef = useRef(null);
  const composerKind = COMPOSER_KIND[tab];

  // 왼쪽 메뉴 "게시글 작성" 모달에서 올린 글을 피드 맨 위에 넣는다.
  useEffect(() => {
    if (!composedPost) return;
    upsertPost(composedPost);
    setTab('recommend');
    clearComposedPost();
  }, [composedPost, clearComposedPost, upsertPost]);

  const followingUnavailable = tab === 'following' && !loggedIn;

  // 맨 위로 올라가면서 지금 탭을 새로 불러와 새로 올라온 글을 보여 준다.
  function goTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    refresh();
  }
  // 왼쪽 메뉴에서 홈을 다시 눌러도 같은 동작을 한다.
  useEffect(() => onFeedTopRequest(goTop));

  return (
    <>
      <MainColumn>
        <Tabs items={TABS} value={tab} onChange={setTab} label="피드 보기" sticky />
        {composerKind && (
          <Composer
            ref={composerRef}
            kind={composerKind}
            onPosted={(post) => { upsertPost(post); }}
          />
        )}
        {tab === 'tech' && (
          <div style={{ padding: '12px 20px', borderBottom: '1px solid var(--line)', background: 'var(--bg-sub)' }}>
            <ChipRadioGroup options={TECH_FILTERS} value={techFilter} onChange={setTechFilter} label="기술 태그 필터" tone="tech" size="filter" />
          </div>
        )}
        {followingUnavailable && <div className="empty">
          {authLoading ? <p role="status">로그인 상태를 확인하고 있어요.</p> : <>
            <p>팔로잉 피드는 로그인 후 볼 수 있어요.</p>
            <Button to="/login" variant="outline">로그인</Button>
          </>}
        </div>}
        {posts.map((p) => <PostCard key={p.id} post={p} onUpdated={upsertPost} onDeleted={removePost} />)}
        {!followingUnavailable && !posts.length && !error && !loading && (
          <div className="empty">{tab === 'following' ? '팔로우한 사용자의 게시글이 아직 없어요.' : '아직 이 탭에 글이 없어요.'}</div>
        )}
        <div className="empty" aria-live="polite" aria-busy={loading}>
          {loading && <p role="status">게시글을 불러오는 중…</p>}
          {error && <p role="alert">{error}</p>}
          {(hasMore || error) && (
            <Button variant="outline" onClick={loadMore} disabled={loading}>
              {loading ? '불러오는 중…' : error ? '다시 시도' : '게시글 더 보기'}
            </Button>
          )}
        </div>
        <FeedTopButton onClick={goTop} />
      </MainColumn>

      {/* 검색 아래는 추후 활용 영역 */}
      <RightAside showReviews label="검색 및 리뷰 추천">
        <SearchBox />
      </RightAside>
    </>
  );
}
