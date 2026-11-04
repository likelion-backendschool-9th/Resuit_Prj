'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { BADGE_PROGRESS, MAX_PUBLIC_RESUMES, PROFILE_POST } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import Tabs from '../components/common/Tabs.jsx';
import ContributionGraph from '../components/profile/ContributionGraph.jsx';
import PostCard from '../components/feed/PostCard.jsx';
import './ProfilePage.css';

const TABS = [{ id: 'posts', label: '게시글' }, { id: 'feedback', label: '남긴 피드백' }, { id: 'tech', label: '기술 정보' }];

export default function ProfilePage() {
  const { me, posts, commonResumes, publicResumeIds, togglePublicResume } = useAppState();
  const [editing, setEditing] = useState(false);
  const [widgets, setWidgets] = useState({ grass: true, resumes: true });
  const [resumeSettingsOpen, setResumeSettingsOpen] = useState(false);
  const [tab, setTab] = useState('posts');
  const myPosts = posts.filter((p) => p.handle === me.handle && (tab !== 'tech' || p.type === 'tech'));
  const publicResumes = publicResumeIds.map((id) => commonResumes.find((r) => r.id === id)).filter(Boolean);

  return (
    <>
      <MainColumn>
        <div className="profile__banner" aria-hidden="true" />
        <div className="profile__head">
          <div className="profile__head-row">
            <Button to="/profile/verification" variant="outline">기업 이메일 인증</Button>
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
            <label className="check profile__edit-item"><input type="checkbox" checked={widgets.grass} onChange={(e) => setWidgets((w) => ({ ...w, grass: e.target.checked }))} /> GitHub 잔디 (GitHub API 연동)</label>
            <label className="check profile__edit-item">
              <input type="checkbox" checked={widgets.resumes} onChange={(e) => setWidgets((w) => ({ ...w, resumes: e.target.checked }))} />
              <span>공개 이력서 보여 주기 <span className="xs muted">(어떤 이력서를 보일지는 아래 "공개 이력서"의 공개 설정에서)</span></span>
            </label>
            <label className="check profile__edit-item muted"><input type="checkbox" disabled /> CLI 도구 활동 (여유 시 추가)</label>
          </section>
        )}

        {widgets.grass && (
          <section className="card profile__widget">
            <div className="row-wrap" style={{ justifyContent: 'space-between', gap: 12 }}>
              <h2>GitHub 기여 기록</h2>
              <span className="small muted">github.com/[아이디] · 최근 1년</span>
            </div>
            <ContributionGraph />
          </section>
        )}

        {widgets.resumes && (
          <section className="card profile__widget" style={{ gap: 10 }}>
            <div className="row" style={{ justifyContent: 'space-between', gap: 12 }}>
              <h2>공개 이력서 <span className="small muted" style={{ fontWeight: 400 }}>{publicResumes.length}개</span></h2>
              <Button
                variant={resumeSettingsOpen ? 'primary' : 'accent-outline'}
                size="mid"
                className="profile__settings-btn"
                aria-expanded={resumeSettingsOpen}
                onClick={() => setResumeSettingsOpen((v) => !v)}
              >
                <Icon name="eye" size={16} />{resumeSettingsOpen ? '설정 완료' : '공개 설정'}
              </Button>
            </div>

            {resumeSettingsOpen && (
              <fieldset className="profile__resume-settings">
                <legend>프로필에 공개할 공통 이력서 (최대 {MAX_PUBLIC_RESUMES}개)</legend>
                {commonResumes.map((r) => {
                  const order = publicResumeIds.indexOf(r.id);
                  const on = order >= 0;
                  return (
                    <label key={r.id} className="check" style={{ gap: 10, fontSize: 14, cursor: 'pointer' }}>
                      <input type="checkbox" checked={on} disabled={!on && publicResumeIds.length >= MAX_PUBLIC_RESUMES} onChange={() => togglePublicResume(r.id)} />
                      <span className="grow"><strong>{r.name}</strong> <span className="muted">· {r.field} · {r.updated}</span></span>
                      <span className={`profile__order${on ? ' is-on' : ''}`}>{on ? order + 1 : ''}</span>
                    </label>
                  );
                })}
                <div className="row-wrap xs muted" style={{ justifyContent: 'space-between' }}>
                  <span>{publicResumes.length} / {MAX_PUBLIC_RESUMES}개 공개 · 체크한 순서대로 보여요</span>
                  <Link href="/resumes" className="text-link" style={{ fontSize: 12 }}>이력서 이름 바꾸기 · 새로 만들기</Link>
                </div>
              </fieldset>
            )}

            {publicResumes.length === 0 && (
              <div className="profile__no-resume">아직 공개한 이력서가 없어요. "공개 설정"에서 프로필에 보일 이력서를 골라 주세요.</div>
            )}

            {publicResumes.map((r) => (
              <div key={r.id} className="profile__resume">
                <div className="resume-thumb" aria-hidden="true"><span /><span /><span /></div>
                <div className="grow"><div className="bold">{r.name}</div><div className="small muted">{r.field} · {r.sections}</div></div>
                <Link href="/resumes" className="text-link">보기</Link>
              </div>
            ))}
          </section>
        )}

        <div style={{ marginTop: 16 }}><Tabs items={TABS} value={tab} onChange={setTab} label="프로필 탭" /></div>
        {tab === 'feedback' && <div className="empty">남긴 피드백은 피드백 게시판과 연결될 예정이에요.</div>}
        {tab !== 'feedback' && myPosts.map((p) => <PostCard key={p.id} post={p} />)}
        {tab === 'posts' && (
          <article className="post-card" style={{ padding: '16px 24px' }}>
            <Avatar initial={me.initial} color={me.av} />
            <div className="grow">
              <div><strong>{me.name}</strong> <span className="muted">@{me.handle} · {PROFILE_POST.time}</span></div>
              <p style={{ marginTop: 6 }}>{PROFILE_POST.body}</p>
            </div>
          </article>
        )}
        {tab === 'tech' && myPosts.length === 0 && <div className="empty">아직 공유한 기술 글이 없어요. 홈에서 첫 글을 올려 보세요.</div>}
      </MainColumn>

      <RightAside>
        <section className="card profile__badge-card" aria-labelledby="badge-h">
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <h2 id="badge-h">리뷰어 인증 배지</h2>
            <Badge tone="general">진행 중</Badge>
          </div>
          <div className="text-2" style={{ fontSize: 14 }}>
            목표 <strong style={{ color: 'var(--text)' }}>{BADGE_PROGRESS.goal}</strong> · 다음 평가 <strong style={{ color: 'var(--text)' }}>{BADGE_PROGRESS.nextEval}</strong> ({BADGE_PROGRESS.dday})
          </div>
          {BADGE_PROGRESS.metrics.map((m) => (
            <div key={m.label} className="stack" style={{ gap: 6 }}>
              <div className="row small" style={{ justifyContent: 'space-between' }}><span>{m.label}</span><strong>{m.value} / {m.max}{m.unit}</strong></div>
              <div className="progress" role="progressbar" aria-label={m.label} aria-valuemin={0} aria-valuemax={m.max} aria-valuenow={m.value}>
                <div style={{ width: `${(m.value / m.max) * 100}%` }} />
              </div>
            </div>
          ))}
          <p className="small bold" style={{ color: 'var(--accent-text)' }}>{BADGE_PROGRESS.remaining}</p>
          <div className="profile__badge-note">최근 2분기 활동을 합산해 분기마다 평가해요. 기준에 못 미치면 1분기 동안 "갱신 필요" 상태로 유예되고, 그동안은 기업 검색 필터에 잡히지 않아요.</div>
          <div className="stack" style={{ gap: 4 }}>
            <span className="xs bold text-2">배지 이력</span>
            <span className="small muted">아직 받은 배지가 없어요.</span>
          </div>
          <Button variant="accent-outline" to="/feedback">피드백하러 가기</Button>
        </section>
        <section className="card" style={{ gap: 8 }}>
          <h2>연동 계정</h2>
          <div className="linked"><span className="linked__dot" aria-hidden="true" />GitHub 연결됨 · {me.github}</div>
          <div className="linked"><span className="linked__dot" aria-hidden="true" />벨로그 RSS 연결됨 · @seoyeon</div>
          <p className="small muted">기여 기록은 하루 한 번, 블로그 새 글은 RSS로 주기적으로 가져와요.</p>
          <Link href="/onboarding" className="text-link">연동 관리</Link>
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
