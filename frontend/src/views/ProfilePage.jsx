'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import FollowButton from '../components/common/FollowButton.jsx';
import Icon from '../components/common/Icon.jsx';
import Tabs from '../components/common/Tabs.jsx';
import ContributionGraph from '../components/profile/ContributionGraph.jsx';
import PostCard from '../components/feed/PostCard.jsx';
import useProfileData from '../components/profile/useProfileData.js';
import './ProfilePage.css';

const TABS = [
  { id: 'posts', label: '게시글' },
  { id: 'feedback', label: '남긴 피드백' },
  { id: 'tech', label: '기술 정보' },
];

const MAX_PUBLIC_RESUMES = 3;

/** 오른쪽 보유 배지 미리보기 개수. 나머지는 더 보기로 펼친다. */
const BADGE_PREVIEW_LIMIT = 3;

/** 프로필 헤더에 바로 보여줄 경력 수. 나머지는 더 보기로 펼친다. */
const CAREER_PREVIEW_LIMIT = 2;

const BADGE_STATUS_ORDER = { ACTIVE: 0, GRACE: 1, EXPIRED: 2 };

const CURRENT_YEAR = new Date().getFullYear();
/** GitHub 조회는 한 번에 1년이라 연도 단위로 고른다. */
const GRASS_YEARS = Array.from({ length: 15 }, (_, index) => CURRENT_YEAR - index);

function pad2(value) {
  return String(value).padStart(2, '0');
}

function toIsoDate(date) {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
}

/** 선택한 연도의 1.1 ~ 12.31 (올해면 오늘까지). */
function contributionRangeForYear(year) {
  const today = new Date();
  const from = `${year}-01-01`;
  const to = year === today.getFullYear() ? toIsoDate(today) : `${year}-12-31`;
  return { from, to };
}

function formatGrassRangeLabel(contributions, year) {
  if (contributions?.from && contributions?.to) {
    return `${contributions.from} ~ ${contributions.to}`;
  }
  return `${year}년`;
}

function careerPeriod(item) {
  const start = (item.startDate || '').replaceAll('-', '.');
  const end = item.endDate ? String(item.endDate).replaceAll('-', '.') : '재직 중';
  return start ? `${start} ~ ${end}` : end;
}

function statusLabel(status) {
  if (status === 'ACTIVE') return '활성';
  if (status === 'GRACE') return '갱신 필요';
  if (status === 'EXPIRED') return '만료';
  return status || '진행 중';
}

function sortOwnedBadges(badges, displayedId) {
  return [...badges].sort((a, b) => {
    const aDisplayed = a.id === displayedId ? 0 : 1;
    const bDisplayed = b.id === displayedId ? 0 : 1;
    if (aDisplayed !== bDisplayed) return aDisplayed - bDisplayed;
    const aStatus = BADGE_STATUS_ORDER[a.status] ?? 9;
    const bStatus = BADGE_STATUS_ORDER[b.status] ?? 9;
    if (aStatus !== bStatus) return aStatus - bStatus;
    return String(a.name || '').localeCompare(String(b.name || ''), 'ko');
  });
}

export default function ProfilePage({ username, initialProfile = null, startEditing = false }) {
  const { me, loggedIn, authLoading } = useAppState();
  const isOwner = !username || username === me.handle;
  const handle = username || me.handle;
  const [editing, setEditing] = useState(startEditing);
  const [tab, setTab] = useState('posts');
  const [grassYear, setGrassYear] = useState(CURRENT_YEAR);
  const [resumeSettingsOpen, setResumeSettingsOpen] = useState(false);
  const [badgesExpanded, setBadgesExpanded] = useState(false);
  const [careersExpanded, setCareersExpanded] = useState(false);
  const [badgePanels, setBadgePanels] = useState({
    progress: true,
    owned: true,
    history: false,
  });
  const grassRange = contributionRangeForYear(grassYear);
  const data = useProfileData(
    handle,
    isOwner,
    initialProfile,
    tab,
    loggedIn ? me.handle : undefined,
    {
      ready: !authLoading && (loggedIn || Boolean(username)),
      contributionFrom: grassRange.from,
      contributionTo: grassRange.to,
    },
  );

  const toggleBadgePanel = (key) => {
    setBadgePanels((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const profile = data.profile;
  const widgets = useMemo(() => {
    const raw = profile?.widgets;
    if (Array.isArray(raw)) {
      return { grass: raw.includes('GRASS'), resumes: raw.includes('PINNED_RESUME') };
    }
    if (raw && typeof raw === 'object') {
      return { grass: raw.grass !== false, resumes: raw.resumes !== false };
    }
    return { grass: true, resumes: true };
  }, [profile]);

  const displayName = profile?.nickname || profile?.username || me.name;
  const displayHandle = profile?.username || me.handle;
  const intro = profile?.selfIntroduction
    || (loggedIn
      ? '아직 자기소개가 없어요. 프로필 꾸미기에서 소개를 적어 보세요.'
      : '로그인하면 내 프로필을 볼 수 있어요.');
  const pinnedResume = profile?.pinnedResume;
  const ownedBadges = useMemo(
    () => sortOwnedBadges(profile?.badges || [], profile?.displayedBadge?.id),
    [profile?.badges, profile?.displayedBadge?.id],
  );
  const visibleBadges = badgesExpanded
    ? ownedBadges
    : ownedBadges.slice(0, BADGE_PREVIEW_LIMIT);
  const hiddenBadgeCount = Math.max(0, ownedBadges.length - BADGE_PREVIEW_LIMIT);
  const careers = profile?.careers || [];
  const visibleCareers = careersExpanded ? careers : careers.slice(0, CAREER_PREVIEW_LIMIT);
  const hiddenCareerCount = Math.max(0, careers.length - CAREER_PREVIEW_LIMIT);
  const badgeHistory = profile?.badgeHistory || [];
  const progress = data.progress;
  const apiPosts = tab === 'tech' ? data.techPosts : data.posts;
  const ownedResumeList = isOwner ? (data.ownedResumes || []) : [];
  const publicResumes = ownedResumeList.filter((r) => r.isPublic);
  const badgeMetrics = progress ? [
    { label: '채택된 피드백', value: progress.adoptedCount, max: 5 },
    { label: '서로 다른 요청자', value: progress.distinctRequesters, max: 3 },
    { label: '베스트 리뷰', value: progress.bestReviewCount, max: 1 },
  ] : [
    { label: '채택된 피드백', value: 0, max: 5 },
    { label: '서로 다른 요청자', value: 0, max: 3 },
    { label: '베스트 리뷰', value: 0, max: 1 },
  ];
  const github = data.integrations?.find((item) => item.provider === 'GITHUB');
  const blog = data.integrations?.find((item) => item.provider === 'BLOG');

  const setWidget = async (key, checked) => {
    if (!isOwner) return;
    const next = {
      grass: key === 'grass' ? checked : widgets.grass,
      resumes: key === 'resumes' ? checked : widgets.resumes,
    };
    try {
      await data.updateProfile({ widgets: next });
    } catch {
      data.setError('위젯 설정을 저장하지 못했습니다.');
    }
  };

  const toggleResumePublic = async (resume) => {
    const publicCount = ownedResumeList.filter((r) => r.isPublic).length;
    if (!resume.isPublic && publicCount >= MAX_PUBLIC_RESUMES) {
      data.setError(`공개 이력서는 최대 ${MAX_PUBLIC_RESUMES}개까지 가능합니다.`);
      return;
    }
    try {
      await data.setVisibility(resume.id, !resume.isPublic);
      data.setError('');
    } catch {
      data.setError('이력서 공개 설정을 저장하지 못했습니다.');
    }
  };

  return (
    <>
      <MainColumn>
        <div className="profile__banner" aria-hidden="true" />
        <div className="profile__head">
          <div className="profile__head-row">
            {isOwner ? (
              <Button to="/profile/verification" variant="outline">기업 이메일 인증</Button>
            ) : (
              <FollowButton
                handle={displayHandle}
                initialFollowing={profile?.isFollowing}
                offVariant="outline"
                onChanged={(status) => data.patchProfile({
                  isFollowing: !!status.isFollowing,
                  followers: status.followerCount ?? profile?.followers,
                  following: status.followingCount ?? profile?.following,
                })}
              />
            )}
            <div className="profile__avatar">
              <Avatar
                initial={(displayName || '?').slice(0, 1)}
                color={me.av}
                size={120}
                src={profile?.profileImageUrl || undefined}
              />
            </div>
            {isOwner ? (
              <Button variant="outline" aria-pressed={editing} onClick={() => setEditing((v) => !v)}>
                {editing ? '꾸미기 완료' : '프로필 꾸미기'}
              </Button>
            ) : <span />}
          </div>
          <div>
            <div className="profile__name-row">
              <div className="profile__display-name">{displayName}</div>
              {profile?.companyVerified && (
                <Badge tone="company" size="md">현직자 인증</Badge>
              )}
            </div>
            <div className="muted">@{displayHandle}</div>
          </div>
          <p>{profile?.selfIntroduction || intro}</p>
          {careers.length > 0 && (
            <div className="profile__careers">
              <div className="profile__career-root">경력/</div>
              <ul className="profile__career-tree">
                {visibleCareers.map((item, index) => {
                  const isLast = index === visibleCareers.length - 1 && hiddenCareerCount === 0;
                  return (
                    <li key={item.id} className={`profile__career-node${isLast ? ' is-last' : ''}`}>
                      <span className="profile__career-name">{item.companyName} · {item.jobTitle}</span>
                      <ul className="profile__career-tree">
                        <li className={`profile__career-node${item.description ? '' : ' is-last'}`}>
                          <span className="profile__career-meta">{careerPeriod(item)}</span>
                        </li>
                        {item.description && (
                          <li className="profile__career-node is-last">
                            <p className={`profile__career-desc${careersExpanded ? '' : ' is-clamped'}`}>{item.description}</p>
                          </li>
                        )}
                      </ul>
                    </li>
                  );
                })}
                {hiddenCareerCount > 0 && (
                  <li className="profile__career-node is-last">
                    <button
                      type="button"
                      className="profile__career-more"
                      aria-expanded={careersExpanded}
                      onClick={() => setCareersExpanded((open) => !open)}
                    >
                      {careersExpanded ? '경력 접기' : `경력 더 보기 (${hiddenCareerCount})`}
                    </button>
                  </li>
                )}
              </ul>
            </div>
          )}
          <div className="profile__stats">
            <span><strong>{profile?.following ?? 0}</strong> 팔로잉</span>
            <span><strong>{profile?.followers ?? 0}</strong> 팔로워</span>
            <span><strong>{profile?.feedbacks ?? 0}</strong> 남긴 피드백</span>
            <span><strong>{profile?.posts ?? 0}</strong> 게시글</span>
          </div>
          {profile?.displayedBadge && (
            <div className="profile__sharer">
              <Badge tone="sharer" size="md">{profile.displayedBadge.name}</Badge>
            </div>
          )}
        </div>

        {data.loading && <p className="profile__message">프로필을 불러오는 중…</p>}
        {data.error && <p className="profile__message" role="status">{data.error}</p>}

        {editing && isOwner && (
          <section className="profile__edit">
            <h2>기본 정보</h2>
            <p className="small muted">닉네임·소개·프로필 사진은 프로필 편집에서 수정할 수 있어요.</p>
            <Link href="/setting/profile" className="text-link">프로필 편집으로 이동</Link>

            <div className="profile__setting-divider" />
            <h2>프로필 위젯</h2>
            <label className="check profile__edit-item">
              <input type="checkbox" checked={widgets.grass} onChange={(e) => setWidget('grass', e.target.checked)} />
              GitHub 잔디 보여 주기
            </label>
            <label className="check profile__edit-item">
              <input type="checkbox" checked={widgets.resumes} onChange={(e) => setWidget('resumes', e.target.checked)} />
              <span>
                대표 이력서 보여 주기
                <span className="xs muted"> (공개/고정은 아래에서)</span>
              </span>
            </label>

            <div className="profile__setting-divider" />
            <h2>대표 레포·블로그</h2>
            <p className="small muted">
              대표 레포는 연동 관리에서 GitHub 공개 레포를 최대 3개까지 고를 수 있어요.
            </p>
            <Link href="/setting/integrations" className="text-link">연동 관리에서 레포·블로그 설정</Link>
          </section>
        )}

        {widgets.grass && (
          <section className="card profile__widget">
            <div className="row-wrap" style={{ justifyContent: 'space-between', gap: 12, alignItems: 'center' }}>
              <h2>GitHub 기여 기록</h2>
              <label className="profile__grass-period">
                <span className="sr-only">기여 연도</span>
                <select
                  value={grassYear}
                  onChange={(event) => setGrassYear(Number(event.target.value))}
                  aria-label="기여 잔디 연도"
                >
                  {GRASS_YEARS.map((year) => (
                    <option key={year} value={year}>{year}</option>
                  ))}
                </select>
              </label>
            </div>
            <p className="small muted" style={{ margin: 0 }}>
              {`github.com/${data.contributions?.login || github?.externalId || '[아이디]'}`}
              {` · ${formatGrassRangeLabel(data.contributions, grassYear)}`}
              {typeof data.contributions?.totalContributions === 'number'
                ? ` · ${data.contributions.totalContributions}회`
                : ''}
            </p>
            <ContributionGraph
              weeks={data.contributions?.weeks}
              label={`${formatGrassRangeLabel(data.contributions, grassYear)} GitHub 기여 기록`}
            />
            {!data.contributions?.weeks && (
              <p className="small muted">
                {loggedIn
                  ? 'GitHub 기여 기록을 불러오지 못했어요. GitHub로 다시 로그인해 연동을 새로고침해 보세요.'
                  : 'GitHub로 로그인하면 기여 잔디를 볼 수 있어요.'}
              </p>
            )}
          </section>
        )}

        {widgets.resumes && (
          <section className="card profile__widget" style={{ gap: 10 }}>
            <div className="row" style={{ justifyContent: 'space-between', gap: 12 }}>
              <h2>
                대표 이력서{' '}
                <span className="small muted" style={{ fontWeight: 400 }}>
                  {pinnedResume ? '1개 고정' : `${publicResumes.length}개 공개`}
                </span>
              </h2>
              {isOwner && (
                <Button
                  variant={resumeSettingsOpen ? 'primary' : 'accent-outline'}
                  size="mid"
                  className="profile__settings-btn"
                  aria-expanded={resumeSettingsOpen}
                  onClick={() => setResumeSettingsOpen((v) => !v)}
                >
                  <Icon name="eye" size={16} />{resumeSettingsOpen ? '설정 완료' : '공개 설정'}
                </Button>
              )}
            </div>

            {isOwner && resumeSettingsOpen && (
              <fieldset className="profile__resume-settings">
                <legend>프로필에 공개할 공통 이력서 (최대 {MAX_PUBLIC_RESUMES}개)</legend>
                {ownedResumeList.length === 0 ? (
                  <p className="small muted">아직 만든 공통 이력서가 없어요. 이력서 관리에서 추가해 주세요.</p>
                ) : ownedResumeList.map((r) => {
                  const title = r.title || r.name;
                  const on = !!r.isPublic;
                  const isPinned = pinnedResume?.id === r.id;
                  const publicOrder = publicResumes.findIndex((item) => item.id === r.id);
                  return (
                    <div key={r.id} className="profile__owned-item">
                      <label className="check profile__resume-choice">
                        <input
                          type="checkbox"
                          checked={on}
                          disabled={!on && publicResumes.length >= MAX_PUBLIC_RESUMES}
                          onChange={() => toggleResumePublic(r)}
                        />
                        <span className="grow">
                          <strong>{title}</strong>
                          <span className="muted"> · {r.field}</span>
                        </span>
                        <span className={`profile__order${on ? ' is-on' : ''}`}>
                          {on && publicOrder >= 0 ? publicOrder + 1 : ''}
                        </span>
                      </label>
                      <div className="profile__owned-actions">
                        <Button
                          size="sm"
                          variant={isPinned ? 'primary' : 'outline'}
                          disabled={!on}
                          onClick={() => data.pinResume(isPinned ? null : r.id)}
                        >
                          {isPinned ? '대표 해제' : '대표로 고정'}
                        </Button>
                      </div>
                    </div>
                  );
                })}
                <div className="row-wrap xs muted" style={{ justifyContent: 'space-between' }}>
                  <span>체크하면 서버에 바로 공개/비공개가 저장됩니다</span>
                  <Link href="/resumes" className="text-link" style={{ fontSize: 12 }}>이력서 관리</Link>
                </div>
              </fieldset>
            )}

            {pinnedResume ? (
              <div className="profile__resume">
                <div className="resume-thumb" aria-hidden="true"><span /><span /><span /></div>
                <div className="grow">
                  <div className="bold">{pinnedResume.title}</div>
                  <div className="small muted">{pinnedResume.field}</div>
                </div>
                <Link href="/resumes" className="text-link">보기</Link>
              </div>
            ) : publicResumes.length === 0 ? (
              <div className="profile__no-resume">아직 공개·고정한 이력서가 없어요.</div>
            ) : (
              publicResumes.map((r) => (
                <div key={r.id} className="profile__resume">
                  <div className="resume-thumb" aria-hidden="true"><span /><span /><span /></div>
                  <div className="grow">
                    <div className="bold">{r.title || r.name}</div>
                    <div className="small muted">{r.field}</div>
                  </div>
                  <Link href="/resumes" className="text-link">보기</Link>
                </div>
              ))
            )}
          </section>
        )}

        <div style={{ marginTop: 16 }}>
          <Tabs items={TABS} value={tab} onChange={setTab} label="프로필 탭" />
        </div>

        {tab === 'feedback' && (
          Array.isArray(data.feedbacks) ? (
            data.feedbacks.length === 0
              ? <div className="empty">아직 남긴 피드백이 없어요.</div>
              : data.feedbacks.map((item) => (
                <article key={item.id} className="card profile__activity">
                  <div className="small muted">요청 #{item.feedbackRequestId} · {item.section || '일반'}</div>
                  <p>{item.content}</p>
                  {item.adopted && <span className="xs bold" style={{ color: 'var(--accent-text)' }}>채택됨</span>}
                </article>
              ))
          ) : <div className="empty">남긴 피드백은 피드백 게시판과 연결될 예정이에요.</div>
        )}

        {tab !== 'feedback' && Array.isArray(apiPosts) && apiPosts.map((p) => (
          <PostCard
            key={p.id}
            post={{
              id: p.id,
              author: displayName,
              handle: displayHandle,
              initial: (displayName || '?').slice(0, 1),
              av: me.av,
              type: p.type,
              body: p.content,
              time: p.createdAt,
              tags: p.tags,
              replies: p.replies,
              reposts: p.reposts,
              likes: p.likes,
            }}
          />
        ))}
        {tab === 'posts' && Array.isArray(apiPosts) && apiPosts.length === 0 && (
          <div className="empty">아직 게시글이 없어요.</div>
        )}
        {tab === 'tech' && Array.isArray(apiPosts) && apiPosts.length === 0 && (
          <div className="empty">아직 공유한 기술 글이 없어요.</div>
        )}
        {data.hasMore?.[tab] && (
          <div className="profile__more">
            <Button variant="outline" onClick={() => data.loadMore(tab)}>더 보기</Button>
          </div>
        )}
      </MainColumn>

      <RightAside>
        <section className="card profile__badge-card" aria-labelledby="badge-h">
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <h2 id="badge-h">리뷰어 인증 배지</h2>
            <Badge tone="general">
              {statusLabel(progress?.reviewerStatus) || '진행 중'}
            </Badge>
          </div>
          <div className="text-2" style={{ fontSize: 14 }}>
            채택 <strong style={{ color: 'var(--text)' }}>{progress?.adoptedCount ?? 0}</strong>
            {' · '}서로 다른 요청자 <strong style={{ color: 'var(--text)' }}>{progress?.distinctRequesters ?? 0}</strong>
          </div>

          <div className="profile__badge-panels">
            <div className="profile__badge-panel">
              <button
                type="button"
                className="profile__badge-panel-toggle"
                aria-expanded={badgePanels.progress}
                onClick={() => toggleBadgePanel('progress')}
              >
                <span>진행</span>
                <Icon
                  name="chevronDown"
                  size={16}
                  className={`profile__badge-chevron${badgePanels.progress ? ' is-open' : ''}`}
                />
              </button>
              {badgePanels.progress && (
                <div className="profile__badge-panel-body">
                  {badgeMetrics.map((m) => (
                    <div key={m.label} className="stack" style={{ gap: 6 }}>
                      <div className="row small" style={{ justifyContent: 'space-between' }}>
                        <span>{m.label}</span>
                        <strong>{m.value} / {m.max}{m.unit || ''}</strong>
                      </div>
                      <div className="progress" role="progressbar" aria-label={m.label} aria-valuemin={0} aria-valuemax={m.max} aria-valuenow={Math.min(m.value, m.max)}>
                        <div style={{ width: `${Math.min(100, (m.value / m.max) * 100)}%` }} />
                      </div>
                    </div>
                  ))}
                  <p className="small bold" style={{ color: 'var(--accent-text)' }}>
                    {progress?.reviewerQualified
                      ? '리뷰어 기준을 충족했어요.'
                      : '채택·요청자·베스트 리뷰 조건을 채워 주세요.'}
                  </p>
                  <div className="profile__badge-note">
                    최근 2분기 활동을 합산해 분기마다 평가해요. 기준에 못 미치면 1분기 동안 &quot;갱신 필요&quot; 상태로 유예되고, 그동안은 기업 검색 필터에 잡히지 않아요.
                  </div>
                </div>
              )}
            </div>

            <div className="profile__badge-panel">
              <button
                type="button"
                className="profile__badge-panel-toggle"
                aria-expanded={badgePanels.owned}
                onClick={() => toggleBadgePanel('owned')}
              >
                <span>
                  보유
                  {ownedBadges.length > 0 ? ` · ${ownedBadges.length}` : ''}
                </span>
                <Icon
                  name="chevronDown"
                  size={16}
                  className={`profile__badge-chevron${badgePanels.owned ? ' is-open' : ''}`}
                />
              </button>
              {badgePanels.owned && (
                <div className="profile__badge-panel-body">
                  {ownedBadges.length === 0 ? (
                    <span className="small muted">아직 받은 배지가 없어요.</span>
                  ) : (
                    <>
                      <div className="profile__owned-list">
                        {visibleBadges.map((badge) => (
                          <div
                            key={badge.id}
                            className={`profile__owned-item${badge.status === 'GRACE' ? ' is-grace' : ''}${badge.status === 'EXPIRED' ? ' is-expired' : ''}${profile?.displayedBadge?.id === badge.id ? ' is-displayed' : ''}`}
                          >
                            <div className="profile__owned-top">
                              <span className="profile__owned-name">{badge.name}</span>
                              <Badge tone="general">{statusLabel(badge.status)}</Badge>
                            </div>
                            {isOwner && badge.status === 'ACTIVE' && (
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => data.displayBadge(
                                  profile?.displayedBadge?.id === badge.id ? null : badge.id,
                                )}
                              >
                                {profile?.displayedBadge?.id === badge.id ? '표시 해제' : '프로필에 표시'}
                              </Button>
                            )}
                          </div>
                        ))}
                      </div>
                      {hiddenBadgeCount > 0 && (
                        <Button
                          variant="outline"
                          size="sm"
                          className="profile__badge-more"
                          aria-expanded={badgesExpanded}
                          onClick={() => setBadgesExpanded((open) => !open)}
                        >
                          {badgesExpanded ? '접기' : `더 보기 (${hiddenBadgeCount})`}
                        </Button>
                      )}
                    </>
                  )}
                </div>
              )}
            </div>

            <div className="profile__badge-panel">
              <button
                type="button"
                className="profile__badge-panel-toggle"
                aria-expanded={badgePanels.history}
                onClick={() => toggleBadgePanel('history')}
              >
                <span>
                  이력
                  {badgeHistory.length > 0 ? ` · ${badgeHistory.length}` : ''}
                </span>
                <Icon
                  name="chevronDown"
                  size={16}
                  className={`profile__badge-chevron${badgePanels.history ? ' is-open' : ''}`}
                />
              </button>
              {badgePanels.history && (
                <div className="profile__badge-panel-body">
                  {badgeHistory.length === 0 ? (
                    <span className="small muted">아직 이력이 없어요.</span>
                  ) : (
                    <div className="profile__history-list">
                      {badgeHistory.map((item, index) => (
                        <div key={`${item.badgeId}-${index}`} className="profile__history-item">
                          <span className="profile__history-name">{item.badgeName}</span>
                          <span className="profile__history-period">
                            {item.activeFrom} ~ {item.activeUntil}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          <Button variant="accent-outline" to="/feedback">피드백하러 가기</Button>
        </section>

        <section className="card" style={{ gap: 8 }}>
          <h2>연동 계정</h2>
          <div className="linked">
            <span className="linked__dot" aria-hidden="true" />
            {github?.connected
              ? `GitHub 연결됨 · ${github.externalId}`
              : (data.contributions?.login
                ? `GitHub · ${data.contributions.login}`
                : 'GitHub 미연결')}
          </div>
          <div className="linked">
            <span className="linked__dot" aria-hidden="true" />
            {blog?.connected ? `블로그 연결됨 · ${blog.platform} · ${blog.externalId}` : '블로그 RSS 미연결'}
          </div>
          <p className="small muted">기여 기록은 하루 한 번, 블로그 새 글은 RSS로 주기적으로 가져와요.</p>
          <Link href={isOwner ? '/setting/integrations' : '/onboarding'} className="text-link">연동 관리</Link>
        </section>

        <section className="card" style={{ gap: 8 }}>
          <h2>대표 레포</h2>
          {(data.repos || []).length === 0 ? (
            <>
              <p className="small muted">아직 고정한 대표 레포가 없어요.</p>
              {isOwner && (
                <Link href="/setting/integrations" className="text-link">
                  연동 관리에서 고르기
                </Link>
              )}
            </>
          ) : data.repos.map((repo) => (
            <div key={repo.fullName || repo.id}>
              <div className="bold">{repo.fullName}</div>
              <div className="small muted">{repo.language || 'Repository'}</div>
            </div>
          ))}
          {isOwner && (data.repos || []).length > 0 && (
            <Link href="/setting/integrations" className="text-link">연동 관리에서 변경</Link>
          )}
        </section>

        <section className="card" style={{ gap: 8 }}>
          <h2>최근 블로그 글</h2>
          {(data.blogPosts || []).length === 0 ? (
            <>
              <p className="small muted">아직 불러온 블로그 글이 없어요.</p>
              {isOwner && (
                <Link href="/setting/integrations" className="text-link">
                  연동 관리에서 블로그 연결
                </Link>
              )}
            </>
          ) : data.blogPosts.map((post) => {
            const published = post.publishedAt
              ? new Date(post.publishedAt).toLocaleDateString('ko-KR', {
                year: 'numeric', month: 'numeric', day: 'numeric',
              })
              : null;
            return (
              <div key={post.id}>
                <div style={{ fontWeight: 500 }}>
                  {post.url ? (
                    <a
                      href={post.url}
                      target="_blank"
                      rel="noreferrer"
                      style={{ color: 'inherit', textDecoration: 'none' }}
                    >
                      {post.title}
                    </a>
                  ) : post.title}
                </div>
                {published && (
                  <div className="xs muted">{published}</div>
                )}
              </div>
            );
          })}
        </section>
      </RightAside>
    </>
  );
}
