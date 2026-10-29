import { useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAppState } from '../context/AppStateContext.jsx';
import { TECH_TAGS } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import { ChipRadioGroup } from '../components/common/Chip.jsx';
import SearchBox from '../components/common/SearchBox.jsx';
import Composer from '../components/feed/Composer.jsx';
import PostCard from '../components/feed/PostCard.jsx';

const TABS = [
  { id: 'all', label: '추천' },
  { id: 'following', label: '팔로잉' },
  { id: 'tech', label: '기술 정보' },
  { id: 'question', label: '질문' }
];
const TECH_FILTERS = [{ id: 'all', label: '전체' }, ...TECH_TAGS.map((t) => ({ id: t, label: `#${t}` }))];

export default function FeedPage() {
  const { posts, following, me } = useAppState();
  const [tab, setTab] = useState('all');
  const [techFilter, setTechFilter] = useState('all');
  const composerRef = useRef(null);
  const location = useLocation();

  // 왼쪽 메뉴 "게시하기"로 들어오면 글쓰기 칸에 포커스
  useEffect(() => {
    if (location.state?.compose) composerRef.current?.focus();
  }, [location.state]);

  const visible = posts.filter((p) => {
    if (tab === 'all') return true;
    if (tab === 'following') return !!following[p.handle] || p.handle === me.handle;
    if (tab === 'tech') return p.type === 'tech' && (techFilter === 'all' || p.tags?.includes(techFilter));
    return p.type === tab;
  });

  return (
    <>
      <MainColumn>
        <Tabs items={TABS} value={tab} onChange={setTab} label="피드 보기" sticky />
        <Composer ref={composerRef} onPosted={() => setTab('all')} />
        {tab === 'tech' && (
          <div style={{ padding: '12px 20px', borderBottom: '1px solid var(--line)', background: 'var(--bg-sub)' }}>
            <ChipRadioGroup options={TECH_FILTERS} value={techFilter} onChange={setTechFilter} label="기술 태그 필터" tone="tech" size="sm" />
          </div>
        )}
        {visible.length ? visible.map((p) => <PostCard key={p.id} post={p} />) : <div className="empty">아직 이 탭에 글이 없어요.</div>}
      </MainColumn>

      {/* 검색 아래는 추후 활용 영역 */}
      <RightAside label="검색 및 추후 활용 영역">
        <SearchBox />
      </RightAside>
    </>
  );
}
