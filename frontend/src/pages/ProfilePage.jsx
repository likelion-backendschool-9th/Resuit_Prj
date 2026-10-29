import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAppState } from '../context/AppStateContext.jsx';
import { COMMON_RESUME } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Tabs from '../components/common/Tabs.jsx';
import ContributionGraph from '../components/profile/ContributionGraph.jsx';
import PostCard from '../components/feed/PostCard.jsx';
import './ProfilePage.css';

const TABS = [{ id: 'posts', label: '게시글' }, { id: 'feedback', label: '남긴 피드백' }, { id: 'tech', label: '기술 정보' }];

export default function ProfilePage() {
  const { me, posts } = useAppState();
  const [editing, setEditing] = useState(false);
  const [widgets, setWidgets] = useState({ grass: true, pinned: true });
  const [tab, setTab] = useState('posts');
  const myPosts = posts.filter((p) => p.handle === me.handle && (tab !== 'tech' || p.type === 'tech'));

  return (
    <>
      <MainColumn>
        <div className="profile__banner" aria-hidden="true" />
        <div className="profile__head">
          <div className="profile__head-row">
            <div className="profile__avatar"><Avatar initial={me.initial} color={me.av} size={120} /></div>
            <Button variant="outline" aria-pressed={editing} onClick={() => setEditing((v) => !v)}>{editing ? '꾸미기 완료' : '프로필 꾸미기'}</Button>
          </div>
          <div><div style={{ fontSize: 22, fontWeight: 900 }}>{me.name}</div><div className="muted">@{me.handle}</div></div>
          <p>사용자가 편한 화면을 고민하는 프론트엔드 개발자 지망생. React와 TypeScript를 주로 씁니다.</p>
          <div className="row-wrap">{['React', 'TypeScript', 'Next.js'].map((s) => <span key={s} className="skill-tag">{s}</span>)}</div>
          <div className="profile__stats">
            <span><strong>128</strong> 팔로잉</span><span><strong>96</strong> 팔로워</span>
            <span><strong>23</strong> 남긴 피드백</span><span><strong>6</strong> 공유한 기술 글</span>
          </div>
          <div className="profile__sharer">
            <Badge tone="sharer" size="md">React 공유자</Badge>
            <span className="small" style={{ color: 'var(--tech)' }}>주로 공유하는 분야 · #React #TypeScript</span>
          </div>
        </div>

        {editing && (
          <section className="profile__edit">
            <h2>프로필 위젯</h2>
            <label className="check"><input type="checkbox" checked={widgets.grass} onChange={(e) => setWidgets((w) => ({ ...w, grass: e.target.checked }))} /> GitHub 잔디 (GitHub API 연동)</label>
            <label className="check"><input type="checkbox" checked={widgets.pinned} onChange={(e) => setWidgets((w) => ({ ...w, pinned: e.target.checked }))} /> 대표 이력서 고정</label>
            <label className="check muted"><input type="checkbox" disabled /> CLI 도구 활동 (여유 시 추가)</label>
          </section>
        )}

        {widgets.grass && (
          <section className="card profile__widget">
            <div className="row-wrap" style={{ justifyContent: 'space-between' }}>
              <h2>GitHub 기여 기록</h2>
              <span className="small muted">github.com/{me.github} · 최근 1년</span>
            </div>
            <ContributionGraph />
          </section>
        )}

        {widgets.pinned && (
          <section className="card profile__widget" style={{ flexDirection: 'row', alignItems: 'center', gap: 14 }}>
            <div className="resume-thumb" aria-hidden="true"><span /><span /><span /></div>
            <div className="grow"><div className="xs muted">고정된 대표 이력서</div><div className="bold">{COMMON_RESUME.title}</div></div>
            <Link to="/resumes" className="text-link">보기</Link>
          </section>
        )}

        <div style={{ marginTop: 16 }}><Tabs items={TABS} value={tab} onChange={setTab} label="프로필 탭" /></div>
        {tab === 'feedback'
          ? <div className="empty">남긴 피드백은 피드백 게시판과 연결될 예정이에요.</div>
          : myPosts.length ? myPosts.map((p) => <PostCard key={p.id} post={p} />) : <div className="empty">아직 작성한 글이 없어요. 홈에서 첫 글을 올려 보세요.</div>}
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>연동 계정</h2>
          <div className="linked"><span className="linked__dot" aria-hidden="true" />GitHub 연결됨 · {me.github}</div>
          <div className="linked"><span className="linked__dot" aria-hidden="true" />벨로그 RSS 연결됨 · @seoyeon</div>
          <p className="small muted">기여 기록은 하루 한 번, 블로그 새 글은 RSS로 주기적으로 가져와요.</p>
          <Link to="/onboarding" className="text-link">연동 관리</Link>
        </section>
        <section className="card">
          <h2>대표 레포</h2>
          <div><div className="bold">study-match-web</div><div className="small muted">TypeScript · 이력서 프로젝트에 연결됨</div></div>
          <div><div className="bold">button-kit</div><div className="small muted">TypeScript · 이력서 프로젝트에 연결됨</div></div>
        </section>
        <section className="card">
          <h2>최근 블로그 글</h2>
          <div><div style={{ fontWeight: 500 }}>디자인 시스템 버튼 컴포넌트를 만들며 배운 것</div><div className="xs bold" style={{ color: 'var(--blog)' }}>벨로그 · 3일 전</div></div>
          <div><div style={{ fontWeight: 500 }}>무한 스크롤 구현기</div><div className="xs bold" style={{ color: 'var(--blog)' }}>벨로그 · 2주 전</div></div>
        </section>
      </RightAside>
    </>
  );
}
