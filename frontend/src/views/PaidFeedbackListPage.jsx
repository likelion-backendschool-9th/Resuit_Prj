'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Badge from '@/components/common/Badge.jsx';
import Button from '@/components/common/Button.jsx';
import Tabs from '@/components/common/Tabs.jsx';
import PaidFeedbackItem from '@/components/paidFeedback/PaidFeedbackItem.jsx';
import { STATUS } from '@/components/paidFeedback/labels.js';
import { useAppState } from '@/context/AppStateContext.jsx';
import { getPaidFeedbacks } from '@/services/paidFeedback.js';

// 내 유료 피드백(8.4). 서버는 내가 요청한 것과 현직자로 받은 것을 함께 주고, 화면에서 탭으로 나눈다.
// 탭은 페이지 안 상태로만 바꾼다(주소는 그대로).
export default function PaidFeedbackListPage() {
  const { me } = useAppState();
  const [tab, setTab] = useState('sent');
  const [items, setItems] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    getPaidFeedbacks({ page: 0, size: 100 })
      .then((data) => { if (active) setItems(data.content); })
      .catch((err) => { if (active) setError(err); });
    return () => { active = false; };
  }, []);

  const sent = (items || []).filter((item) => item.requester.username === me?.handle);
  const received = (items || []).filter((item) => item.mentor.username === me?.handle);
  const list = tab === 'sent' ? sent : received;
  const tabs = [
    { id: 'sent', label: <>보낸 요청 <span className="xs muted">{sent.length}</span></> },
    { id: 'received', label: <>받은 요청 <span className="xs muted">{received.length}</span></> }
  ];

  return <>
    <MainColumn>
      <div className="page-title"><h1>내 유료 피드백</h1><Button to="/mentors" variant="outline" size="sm">현직자 찾기</Button></div>
      <p className="page-desc">보낸 요청과, 현직자라면 받은 요청을 모아 봐요.</p>
      <Tabs items={tabs} value={tab} onChange={setTab} label="유료 피드백 구분" />
      {error?.status === 401 ? (
        <div className="empty"><p>로그인한 뒤 볼 수 있어요.</p><Button to="/login" size="sm">로그인하기</Button></div>
      ) : error ? (
        <div className="empty" role="alert">{error.message}</div>
      ) : items === null ? (
        <div className="empty">불러오는 중…</div>
      ) : list.length === 0 ? (
        <div className="empty">{tab === 'sent' ? '아직 보낸 요청이 없어요. 현직자 피드백에서 요청해 보세요.' : '받은 요청이 없어요. 현직자로 등록하면 요청을 받을 수 있어요.'}</div>
      ) : (
        list.map((item) => <PaidFeedbackItem key={item.id} item={item} box={tab} />)
      )}
    </MainColumn>
    <RightAside label="상태 안내">
      <section className="card" style={{ gap: 8 }}>
        <h2>상태 안내</h2>
        {Object.entries(STATUS).map(([key, s]) => <div key={key} className="row" style={{ gap: 8 }}><Badge tone={s.tone}>{s.label}</Badge></div>)}
        <p className="xs muted">현직자가 첫 답변을 등록하면 완료되고 현직자에게 크레딧이 적립돼요.</p>
      </section>
    </RightAside>
  </>;
}
