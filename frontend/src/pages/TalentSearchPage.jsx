import { useState } from 'react';
import { TALENTS } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import Tabs from '../components/common/Tabs.jsx';
import TalentCard from '../components/talent/TalentCard.jsx';
import TalentFilters from '../components/talent/TalentFilters.jsx';
import './TalentSearchPage.css';

const FIELDS = [{ id: 'all', label: '전체' }, { id: '백엔드', label: '백엔드' }, { id: '프론트엔드', label: '프론트엔드' }, { id: '풀스택', label: '풀스택' }];
const NO_FILTERS = { badgeOnly: false, skills: [], career: 'all', streak: false, github: false, writer: false, region: 'all' };
const MIN_STREAK = 2;
const MIN_GITHUB = 500;
const MIN_POSTS = 5;

const activeBadges = (p) => p.badges.filter((b) => b.status === 'active');

function matches(p, f) {
  return (!f.badgeOnly || activeBadges(p).length > 0)
    && (!f.streak || activeBadges(p).some((b) => b.streak >= MIN_STREAK))
    && f.skills.every((k) => p.skills.includes(k))
    && (f.career === 'all' || p.career === f.career)
    && (!f.github || p.github >= MIN_GITHUB)
    && (!f.writer || p.posts >= MIN_POSTS)
    && (f.region === 'all' || p.region === f.region);
}

function activeLabels(f) {
  return [
    f.badgeOnly && '인증 배지',
    ...f.skills,
    f.career !== 'all' && f.career,
    f.streak && `연속 인증 ${MIN_STREAK}분기+`,
    f.github && `GitHub ${MIN_GITHUB}+`,
    f.writer && `기술 글 ${MIN_POSTS}+`,
    f.region !== 'all' && f.region
  ].filter(Boolean);
}

// 기업 회원 전용: 공개 이력서 검색. 상세 필터는 기업 Pro에서 열린다.
export default function TalentSearchPage() {
  const [pro, setPro] = useState(false);
  const [tab, setTab] = useState('all');
  const [filters, setFilters] = useState(NO_FILTERS);
  const [lockMessage, setLockMessage] = useState(false);

  const count = (id) => TALENTS.filter((p) => id === 'all' || p.field === id).length;
  const tabs = FIELDS.map((f) => ({ id: f.id, label: <>{f.label} <span className="xs muted">{count(f.id)}</span></> }));

  const byField = TALENTS.filter((p) => tab === 'all' || p.field === tab);
  const applied = pro ? filters : NO_FILTERS;
  const people = byField.filter((p) => matches(p, applied));
  const labels = activeLabels(applied);

  const togglePro = () => {
    setPro((v) => !v);
    setLockMessage(false);
  };

  return (
    <>
      <MainColumn>
        <div className="page-title" style={{ alignItems: 'center', justifyContent: 'flex-start', gap: 10 }}>
          <h1>인재 찾기</h1>
          <Badge tone="invert" size="md">기업 회원 전용</Badge>
          <span className="small muted" style={{ marginLeft: 'auto' }}>
            노바랩스 · <strong style={{ color: pro ? 'var(--accent-text)' : 'var(--text)' }}>{pro ? '기업 Pro 체험 중' : '무료'}</strong>
          </span>
        </div>
        <p className="page-desc">구직자가 프로필에서 공개로 설정한 이력서만 보여요. 인증 배지는 분기마다 갱신되고, 다음 평가는 2027년 1월 1일이에요.</p>

        <Tabs items={tabs} value={tab} onChange={setTab} label="분야" />

        <div className="talent-result">
          <span className="text-2" style={{ fontSize: 14 }}>공개 이력서 <strong style={{ color: 'var(--text)' }}>{people.length}</strong>명</span>
          {labels.length > 0 && (
            <div className="row-wrap" style={{ gap: 6 }}>
              {labels.map((label) => <Badge key={label} tone="accent" size="md">{label}</Badge>)}
              <button type="button" className="talent-result__clear" onClick={() => setFilters(NO_FILTERS)}>필터 지우기</button>
            </div>
          )}
        </div>

        {!pro && (
          <div className="talent-teaser">
            <Icon name="lock" size={18} />
            <span className="talent-teaser__text">이 중 <strong>활성 인증 배지 보유자 {byField.filter((p) => activeBadges(p).length > 0).length}명</strong> · 배지 필터와 상세 필터는 기업 Pro에서 쓸 수 있어요.</span>
            <Button variant="dark" size="sm" className="talent-teaser__btn" onClick={togglePro}>Pro 체험하기</Button>
          </div>
        )}

        {people.map((p) => <TalentCard key={p.id} person={p} />)}
        {people.length === 0 && <div className="empty">조건에 맞는 공개 이력서가 없어요. 필터를 줄여 보세요.</div>}
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
          <Button variant={pro ? 'outline' : 'dark'} className="talent-plans__btn" onClick={togglePro}>{pro ? '무료로 돌아가기' : '기업 Pro 체험하기'}</Button>
          <span className="xs muted">실제 결제 없이 등급만 바뀌는 체험이에요. 가격은 [미정].</span>
        </section>
      </RightAside>
    </>
  );
}
