import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAppState } from '../context/AppStateContext.jsx';
import { COMMON_RESUME, SPECIAL_RESUMES } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import './ResumesPage.css';

const TABS = [{ id: 'all', label: '전체' }, { id: 'common', label: '공통' }, { id: 'special', label: '특화' }];
const STATUS = { writing: { label: '작성 중', tone: 'special' }, submitted: { label: '제출 완료', tone: 'accent' } };

export default function ResumesPage() {
  const { myRequested, toggleMyRequest } = useAppState();
  const [tab, setTab] = useState('all');
  const writing = SPECIAL_RESUMES.filter((r) => r.status === 'writing').length;
  const submitted = SPECIAL_RESUMES.filter((r) => r.status === 'submitted').length;

  return (
    <>
      <MainColumn>
        <div className="page-title">
          <h1>내 이력서</h1>
          <Button variant="dark" to="/jobs">새 특화 이력서</Button>
        </div>
        <Tabs items={TABS} value={tab} onChange={setTab} label="이력서 종류" />

        {tab !== 'special' && (
          <section className="section">
            <h2 className="section-label">공통 이력서 · 일반 공고에 그대로 제출</h2>
            <div className="common-resume">
              <div className="resume-thumb common-resume__thumb" aria-hidden="true"><span /><span /><span /><span /></div>
              <div className="grow" style={{ flexBasis: 220 }}>
                <div className="bold" style={{ fontSize: 18 }}>{COMMON_RESUME.title}</div>
                <div className="small muted">{COMMON_RESUME.sections} · 3일 전 수정</div>
                <div className="small" style={{ color: 'var(--accent-text)', fontWeight: 500 }}>특화 이력서 2개의 베이스</div>
              </div>
              <div className="row">
                <Button variant="outline">편집</Button>
                <Button variant={myRequested ? 'outline' : 'primary'} aria-pressed={myRequested} onClick={toggleMyRequest}>
                  {myRequested ? '요청 취소' : '피드백 요청'}
                </Button>
              </div>
            </div>
            {myRequested && (
              <div className="request-notice" role="status">
                <Icon name="check" size={18} />
                <span className="grow">피드백 게시판에 올라갔어요. 다른 사용자들이 섹션별로 피드백을 남길 수 있어요.</span>
                <Link to="/feedback" className="text-link">피드백 보러 가기</Link>
              </div>
            )}
          </section>
        )}

        {tab !== 'common' && (
          <section className="section">
            <h2 className="section-label">특화 이력서 · 공고별로 작성</h2>
            {SPECIAL_RESUMES.map((r) => (
              <Link key={r.id} to={`/resumes/edit?job=${r.jobId || 1}&mode=base`} className="resume-row">
                <Avatar initial={r.initial} color={r.av} company />
                <div className="grow" style={{ flexBasis: 220 }}>
                  <div className="bold">{r.company} · {r.role}</div>
                  <div className="small muted">{r.base} · 요구 문항 {r.questions}개 · {r.updated}</div>
                </div>
                <Badge tone={STATUS[r.status].tone} size="md">{STATUS[r.status].label}</Badge>
              </Link>
            ))}
          </section>
        )}
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>지원 현황</h2>
          <div className="grid-3" style={{ gap: 8 }}>
            <div className="stat"><b>{writing}</b><span className="xs muted">작성 중</span></div>
            <div className="stat"><b>{submitted}</b><span className="xs muted">제출 완료</span></div>
            <div className="stat"><b>0</b><span className="xs muted">결과 발표</span></div>
          </div>
        </section>
        <section className="card">
          <h2>이력서 구조</h2>
          <p className="text-2" style={{ fontSize: 14, lineHeight: 1.7 }}>공통 이력서는 하나, 특화 이력서는 공고마다 하나씩. 특화 이력서는 공통 이력서를 복사해 시작하거나 빈 상태로 시작합니다.</p>
        </section>
      </RightAside>
    </>
  );
}
