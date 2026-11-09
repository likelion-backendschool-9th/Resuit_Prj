'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import Tabs from '../components/common/Tabs.jsx';
import TalentCard from '../components/talent/TalentCard.jsx';
import TalentFilters from '../components/talent/TalentFilters.jsx';
import { CAREER_LABEL, REGION_LABEL, TALENT_FIELDS } from '../components/talent/labels.js';
import { getCompanySubscription, getTalentResume, getTalents, sendCompanyOffer, startProTrial } from '../services/talents.js';
import './TalentSearchPage.css';

const FIELDS = [{ id: 'all', label: '전체' }, ...TALENT_FIELDS];
const NO_FILTERS = { badgeOnly: false, skills: [], career: 'all', streak: false, github: false, writer: false, region: 'all' };
const PAGE_SIZE = 20;
const MIN_STREAK = 2;
const MIN_GITHUB = 500;
const MIN_POSTS = 5;

// 서버 streak은 "지난 분기 수"라서, 이번 분기를 포함한 2분기 연속은 streak=1이다.
function toQuery(tab, f, page) {
  return {
    field: tab === 'all' ? undefined : tab,
    badge: f.badgeOnly ? 'active' : undefined,
    streak: f.streak ? MIN_STREAK - 1 : undefined,
    skills: f.skills,
    career: f.career === 'all' ? undefined : f.career,
    minContrib: f.github ? MIN_GITHUB : undefined,
    minPosts: f.writer ? MIN_POSTS : undefined,
    region: f.region === 'all' ? undefined : f.region,
    page,
    size: PAGE_SIZE
  };
}

function activeLabels(f) {
  return [
    f.badgeOnly && '인증 배지',
    ...f.skills,
    f.career !== 'all' && CAREER_LABEL[f.career],
    f.streak && `연속 인증 ${MIN_STREAK}분기+`,
    f.github && `GitHub ${MIN_GITHUB}+`,
    f.writer && `기술 글 ${MIN_POSTS}+`,
    f.region !== 'all' && REGION_LABEL[f.region]
  ].filter(Boolean);
}

const dateText = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '');

export default function TalentSearchPage() {
  const [subscription, setSubscription] = useState(null);
  const [planError, setPlanError] = useState('');
  const [trialBusy, setTrialBusy] = useState(false);
  const [tab, setTab] = useState('all');
  const [filters, setFilters] = useState(NO_FILTERS);
  const [lockMessage, setLockMessage] = useState(false);
  const [result, setResult] = useState(null);
  const [people, setPeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);

  const pro = !!subscription?.pro;
  // 무료 등급이면 상세 필터를 서버에 보내지 않는다(보내면 403이다).
  const applied = pro ? filters : NO_FILTERS;
  const appliedKey = JSON.stringify(applied);

  useEffect(() => {
    let active = true;
    getCompanySubscription()
      .then((data) => { if (active) setSubscription(data); })
      .catch((err) => { if (active) setPlanError(err.message); });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true); setError(null);
    getTalents(toQuery(tab, JSON.parse(appliedKey), 0))
      .then((data) => {
        if (!active) return;
        setResult(data);
        setPeople(data.content);
      })
      .catch((err) => { if (active) setError(err); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [tab, appliedKey]);

  async function loadMore() {
    setLoadingMore(true);
    try {
      const data = await getTalents(toQuery(tab, applied, result.page + 1));
      setResult(data);
      setPeople((list) => [...list, ...data.content]);
    } catch (err) {
      setError(err);
    } finally {
      setLoadingMore(false);
    }
  }

  async function startTrial() {
    setTrialBusy(true); setPlanError('');
    try {
      setSubscription(await startProTrial());
      setLockMessage(false);
    } catch (err) {
      setPlanError(err.message);
    } finally {
      setTrialBusy(false);
    }
  }

  const counts = result?.fieldCounts || {};
  const tabs = FIELDS.map((f) => ({ id: f.id, label: <>{f.label} <span className="xs muted">{counts[f.id === 'all' ? 'ALL' : f.id] ?? 0}</span></> }));
  const labels = activeLabels(applied);
  const blocked = error && (error.status === 401 || error.status === 403);
  const canTrial = !pro && subscription?.trialAvailable;

  const loadResume = (person) => getTalentResume(person.user.id, person.resumeId);
  const sendOffer = (person, message) => sendCompanyOffer({ userId: person.user.id, message });

  return (
    <>
      <MainColumn>
        <div className="page-title" style={{ alignItems: 'center', justifyContent: 'flex-start', gap: 10 }}>
          <h1>인재 찾기</h1>
          <Badge tone="invert" size="md">기업 회원 전용</Badge>
          {subscription && (
            <span className="small muted" style={{ marginLeft: 'auto' }}>
              <strong style={{ color: pro ? 'var(--accent-text)' : 'var(--text)' }}>
                {pro ? (subscription.trial ? `기업 Pro 체험 중 · ${dateText(subscription.trialEndsAt)}까지` : '기업 Pro') : '무료'}
              </strong>
            </span>
          )}
        </div>
        <p className="page-desc">구직자가 프로필에서 공개로 설정한 이력서만 보여요. 인증 배지는 분기마다 갱신돼요.</p>

        {blocked ? (
          <div className="talent-notice">
            <p className="text-2">{error.status === 401 ? '로그인한 뒤 볼 수 있어요.' : '기업 회원만 볼 수 있어요. 회사 이메일을 인증하면 기업 회원으로 바뀌어요.'}</p>
            <Button to={error.status === 401 ? '/login' : '/profile/verification'} variant="primary" size="sm">
              {error.status === 401 ? '로그인하기' : '기업 이메일 인증하기'}
            </Button>
          </div>
        ) : (
          <>
            <Tabs items={tabs} value={tab} onChange={setTab} label="분야" />

            <div className="talent-result">
              <span className="text-2" style={{ fontSize: 14 }}>공개 이력서 <strong style={{ color: 'var(--text)' }}>{result?.totalElements ?? 0}</strong>명</span>
              {labels.length > 0 && (
                <div className="row-wrap" style={{ gap: 6 }}>
                  {labels.map((label) => <Badge key={label} tone="accent" size="md">{label}</Badge>)}
                  <button type="button" className="talent-result__clear" onClick={() => setFilters(NO_FILTERS)}>필터 지우기</button>
                </div>
              )}
            </div>

            {subscription && !pro && (
              <div className="talent-teaser">
                <Icon name="lock" size={18} />
                <span className="talent-teaser__text">배지 필터와 상세 필터는 <strong>기업 Pro</strong>에서 쓸 수 있어요.</span>
                {canTrial && <Button variant="dark" size="sm" className="talent-teaser__btn" onClick={startTrial} disabled={trialBusy}>Pro 체험하기</Button>}
              </div>
            )}

            {loading && <div className="empty">불러오는 중…</div>}
            {!loading && error && <div className="empty" role="alert">{error.message}</div>}
            {!loading && !error && people.map((p) => <TalentCard key={p.resumeId} person={p} loadResume={loadResume} sendOffer={sendOffer} />)}
            {!loading && !error && people.length === 0 && <div className="empty">조건에 맞는 공개 이력서가 없어요. 필터를 줄여 보세요.</div>}
            {!loading && result?.hasNext && (
              <div className="talent-more">
                <Button variant="outline" size="sm" onClick={loadMore} disabled={loadingMore}>{loadingMore ? '불러오는 중…' : '더 보기'}</Button>
              </div>
            )}
          </>
        )}
      </MainColumn>

      <RightAside>
        <TalentFilters
          pro={pro}
          filters={filters}
          onChange={(patch) => setFilters((f) => ({ ...f, ...patch }))}
          showLockMessage={lockMessage}
          onLocked={() => setLockMessage(true)}
        />
        <section className="card" style={{ gap: 10 }}>
          <h2>기업 요금제</h2>
          <div className="talent-plans">
            <div className={`talent-plan${pro ? '' : ' is-on'}`}><strong>무료</strong><span>분야별 조회</span><span>이력서 열람</span><span>채용 제안</span></div>
            <div className={`talent-plan${pro ? ' is-on' : ''}`}><strong>기업 Pro</strong><span>+ 인증 배지 필터</span><span>+ 기술 · 경력 필터</span><span>+ 연속 인증 · 활동 근거</span><span>+ 지역</span></div>
          </div>
          {canTrial && <Button variant="dark" className="talent-plans__btn" onClick={startTrial} disabled={trialBusy}>{trialBusy ? '체험을 시작하는 중…' : '기업 Pro 14일 체험하기'}</Button>}
          {subscription && !pro && !subscription.trialAvailable && <span className="small muted">체험 기간이 끝났어요.</span>}
          {pro && subscription.trial && <span className="small muted">{dateText(subscription.trialEndsAt)}까지 체험 중이에요.</span>}
          {planError && <p role="alert" className="small talent-card__error">{planError}</p>}
          <Button to="/company/pro" variant="outline" className="talent-plans__btn">기업 Pro 결제하기</Button>
          <span className="xs muted">기업 Pro는 1개월 49,000원, 3개월 129,000원 이용권이에요. 체험은 14일, 한 번만 할 수 있어요.</span>
        </section>
      </RightAside>
    </>
  );
}
