'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { getFeedbackRequests } from '../services/feedbackRequests.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import Button from '../components/common/Button.jsx';
import FeedbackRequestCard from '../components/feedback/FeedbackRequestCard.jsx';

const TABS = [
  { id: 'all', label: '전체' },
  { id: 'FRONTEND', label: '프론트엔드' },
  { id: 'BACKEND', label: '백엔드' },
  { id: 'mine', label: '내 요청' }
];

// 탭을 목록 API 조건으로 바꾼다 (WBS 4.1 전체, 4.3 분야, 4.4 내 요청)
const filterOf = (tab) => (tab === 'mine' ? { mine: true } : tab === 'all' ? {} : { category: tab });

// 이력서 › 피드백: 내 이력서에서 "피드백 요청"한 이력서가 올라오는 게시판
export default function FeedbackPage() {
  const [tab, setTab] = useState('all');
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    getFeedbackRequests(filterOf(tab), controller.signal)
      .then(setList)
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [tab, retry]);

  return (
    <>
      <MainColumn>
        <div className="page-title">
          <h1>이력서 피드백</h1>
          <Link href="/resumes" className="text-link">내 이력서 올리기</Link>
        </div>
        <p className="page-desc">내 이력서에서 "피드백 요청"을 누른 이력서가 여기에 올라와요. 섹션을 골라 구체적으로 남겨 주세요.</p>
        <Tabs items={TABS} value={tab} onChange={setTab} label="분야" />
        {loading ? <div className="empty" role="status">피드백 요청을 불러오는 중이에요.</div>
          : error ? <div className="empty" role="alert"><p>{error}</p><Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>
          : list.length ? list.map((r) => <FeedbackRequestCard key={r.id} request={r} />)
          : <div className="empty">{tab === 'mine' ? '아직 피드백을 요청한 이력서가 없어요. 내 이력서에서 요청해 보세요.' : '이 분야에 올라온 이력서가 아직 없어요.'}</div>}
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>좋은 피드백 쓰는 법</h2>
          <ol className="text-2" style={{ paddingLeft: 18, fontSize: 14, lineHeight: 1.8 }}>
            <li>섹션을 골라 어디에 대한 의견인지 밝히기</li>
            <li>좋은 점 하나, 고칠 점 하나</li>
            <li>고친 예시 문장을 함께 적기</li>
          </ol>
        </section>
      </RightAside>
    </>
  );
}
