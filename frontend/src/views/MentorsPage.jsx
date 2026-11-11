'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import Tabs from '@/components/common/Tabs.jsx';
import MentorCard from '@/components/paidFeedback/MentorCard.jsx';
import { JOB_OPTIONS } from '@/components/paidFeedback/labels.js';
import { getMentors } from '@/services/paidFeedback.js';
import { getCreditBalance } from '@/services/credits.js';

const PAGE_SIZE = 20;
// 직무 탭. 전체는 직무 조건 없이 조회한다.
const TABS = [{ id: 'all', label: '전체' }, ...JOB_OPTIONS];

// 현직자 목록(8.1). 회사 이메일로 인증하고 직무·가격을 저장한 현직자에게 유료 피드백을 요청할 수 있다.
export default function MentorsPage() {
  // 직무 탭(페이지 내부 상태)
  const [tab, setTab] = useState('all');
  const jobTitle = tab === 'all' ? undefined : tab;
  const [result, setResult] = useState(null);
  const [mentors, setMentors] = useState([]);
  const [balance, setBalance] = useState(null);
  const [error, setError] = useState(null);
  const [loadingMore, setLoadingMore] = useState(false);

  // 보유 크레딧은 화면에 들어올 때 한 번만 불러온다.
  useEffect(() => {
    let active = true;
    getCreditBalance().then((data) => { if (active) setBalance(data.balance); }).catch(() => {});
    return () => { active = false; };
  }, []);

  // 탭이 바뀌면 그 직무의 현직자를 첫 페이지부터 다시 불러온다.
  useEffect(() => {
    let active = true;
    setResult(null); setError(null);
    getMentors({ page: 0, size: PAGE_SIZE, jobTitle })
      .then((data) => { if (active) { setResult(data); setMentors(data.content); } })
      .catch((err) => { if (active) setError(err); });
    return () => { active = false; };
  }, [jobTitle]);

  async function loadMore() {
    setLoadingMore(true);
    try {
      const data = await getMentors({ page: result.number + 1, size: PAGE_SIZE, jobTitle });
      setResult(data);
      setMentors((list) => [...list, ...data.content]);
    } catch (err) {
      setError(err);
    } finally {
      setLoadingMore(false);
    }
  }

  return <>
    <MainColumn>
      <div className="page-title"><h1>현직자 피드백</h1></div>
      <p className="page-desc">회사 이메일로 인증한 현직자에게 크레딧을 내고 이력서 피드백을 받아요.</p>
      <Tabs items={TABS} value={tab} onChange={setTab} label="직무" />
      {error?.status === 401 ? (
        <div className="empty"><p>로그인한 뒤 볼 수 있어요.</p><Button to="/login" size="sm">로그인하기</Button></div>
      ) : error ? (
        <div className="empty" role="alert">{error.message}</div>
      ) : result === null ? (
        <div className="empty">불러오는 중…</div>
      ) : mentors.length === 0 ? (
        <div className="empty">{tab === 'all' ? '아직 등록된 현직자가 없어요.' : '이 직무의 현직자가 아직 없어요.'}</div>
      ) : (
        <>
          <p className="small muted" style={{ padding: '12px 24px 0' }}>현직자 {result.totalElements}명</p>
          {mentors.map((mentor) => <MentorCard key={mentor.id} mentor={mentor} />)}
          {!result.last && (
            <div style={{ display: 'flex', justifyContent: 'center', padding: 16 }}>
              <Button variant="outline" size="sm" onClick={loadMore} disabled={loadingMore}>{loadingMore ? '불러오는 중…' : '더 보기'}</Button>
            </div>
          )}
        </>
      )}
    </MainColumn>
    <RightAside label="유료 피드백 안내">
      <section className="card" style={{ gap: 8 }}>
        <h2>유료 피드백이란?</h2>
        <p className="text-2">현직자에게 크레딧을 내고 내 이력서 피드백을 받아요.</p>
        <span className="small muted">보유 크레딧</span>
        <strong style={{ fontSize: 20, color: 'var(--accent-text)' }}>{balance === null ? '…' : `${balance.toLocaleString('ko-KR')} 크레딧`}</strong>
        <Button to="/credits" variant="outline" size="sm">크레딧 충전</Button>
      </section>
      <section className="card" style={{ gap: 6 }}>
        <h2>현직자로 활동하기</h2>
        <p className="text-2">프로필 → 현직자 인증에서 회사 이메일로 인증하고 직무와 가격을 저장하면 여기에 보여요.</p>
        <Button to="/profile/verification" variant="outline" size="sm">현직자 인증</Button>
      </section>
    </RightAside>
  </>;
}
