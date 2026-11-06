'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import FeedbackRequestCard from '../components/feedback/FeedbackRequestCard.jsx';

const TABS = [
  { id: 'all', label: '전체' },
  { id: '프론트엔드', label: '프론트엔드' },
  { id: '백엔드', label: '백엔드' },
  { id: 'mine', label: '내 요청' }
];

// 이력서 › 피드백: 내 이력서에서 "피드백 요청"한 이력서가 올라오는 게시판
export default function FeedbackPage() {
  const { feedbackRequests } = useAppState();
  const [tab, setTab] = useState('all');
  const list = feedbackRequests.filter((r) => tab === 'all' || (tab === 'mine' ? r.mine : r.field === tab));

  return (
    <>
      <MainColumn>
        <div className="page-title">
          <h1>이력서 피드백</h1>
          <Link href="/resumes" className="text-link">내 이력서 올리기</Link>
        </div>
        <p className="page-desc">내 이력서에서 "피드백 요청"을 누른 이력서가 여기에 올라와요. 섹션을 골라 구체적으로 남겨 주세요.</p>
        <Tabs items={TABS} value={tab} onChange={setTab} label="분야" />
        {list.length
          ? list.map((r) => <FeedbackRequestCard key={r.id} request={r} defaultOpen={r.id === 1} />)
          : <div className="empty">{tab === 'mine' ? '아직 피드백을 요청한 이력서가 없어요. 내 이력서에서 요청해 보세요.' : '이 분야에 올라온 이력서가 아직 없어요.'}</div>}
      </MainColumn>

      <RightAside showReviews label="리뷰 추천 및 보조 정보">
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
