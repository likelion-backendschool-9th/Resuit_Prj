'use client';

import { useState } from 'react';
import { JOBS } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import JobCard from '../components/jobs/JobCard.jsx';

export default function JobsPage() {
  const [tab, setTab] = useState('all');
  const count = (t) => JOBS.filter((j) => t === 'all' || j.type === t).length;
  const tabs = [
    { id: 'all', label: `전체 ${count('all')}` },
    { id: 'general', label: `일반 공고 ${count('general')}` },
    { id: 'special', label: `특화 공고 ${count('special')}` }
  ];
  const list = JOBS.filter((j) => tab === 'all' || j.type === tab);

  return (
    <>
      <MainColumn>
        <div className="page-title">
          <h1>채용 공고</h1>
          <span className="small muted">마감 임박순</span>
        </div>
        <p className="page-desc">일반 공고는 공통 이력서로 바로, 특화 공고는 회사 요구 문항에 맞춘 특화 이력서로 지원해요.</p>
        <Tabs items={tabs} value={tab} onChange={setTab} label="공고 종류" />
        <div className="stack" style={{ padding: '16px 24px', gap: 14 }}>
          {list.map((j) => <JobCard key={j.id} job={j} />)}
        </div>
      </MainColumn>
      <RightAside label="추후 활용 영역" />
    </>
  );
}
