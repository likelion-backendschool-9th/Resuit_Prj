'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import OfferItem from '@/components/offers/OfferItem.jsx';
import { getReceivedOffers, getSentOffers } from '@/services/offers.js';

// 화면 종류별 문구와 불러오는 함수. 받은 제안은 일반 회원, 보낸 제안은 기업 회원이 본다.
const BOXES = {
  received: {
    title: '받은 제안',
    desc: '기업 회원이 내 공개 이력서를 보고 보낸 채용 제안이에요.',
    empty: '아직 받은 제안이 없어요. 프로필에서 이력서를 공개하면 기업 회원이 볼 수 있어요.',
    load: getReceivedOffers
  },
  sent: {
    title: '보낸 제안',
    desc: '인재 찾기에서 우리 회사가 보낸 채용 제안이에요.',
    empty: '아직 보낸 제안이 없어요. 인재 찾기에서 채용 제안을 보내 보세요.',
    load: getSentOffers
  }
};

// 채용 제안 목록 화면이다. box: received(받은 제안) | sent(보낸 제안)
export default function OffersPage({ box = 'received' }) {
  const config = BOXES[box];
  const [offers, setOffers] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    config.load()
      .then((data) => { if (active) setOffers(data); })
      .catch((err) => { if (active) setError(err); });
    return () => { active = false; };
  }, [config]);

  return <>
    <MainColumn>
      <div className="page-title"><h1>{config.title}</h1></div>
      <p className="page-desc">{config.desc}</p>
      {error?.status === 401 ? (
        <div className="empty">
          <p>로그인한 뒤 볼 수 있어요.</p>
          <Button to="/login" variant="primary" size="sm">로그인하기</Button>
        </div>
      ) : error ? (
        <div className="empty" role="alert">{error.message}</div>
      ) : offers === null ? (
        <div className="empty">불러오는 중…</div>
      ) : offers.length === 0 ? (
        <div className="empty">{config.empty}</div>
      ) : (
        offers.map((offer) => <OfferItem key={offer.id} offer={offer} box={box} />)
      )}
    </MainColumn>
    <RightAside label={`${config.title} 안내`} />
  </>;
}
