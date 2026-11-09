'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import OfferItem from '@/components/offers/OfferItem.jsx';
import { getReceivedOffers } from '@/services/offers.js';

// 기업 회원이 인재 찾기에서 보낸 채용 제안을 받는 쪽에서 보는 화면이다.
export default function OffersPage() {
  const [offers, setOffers] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    getReceivedOffers()
      .then((data) => { if (active) setOffers(data); })
      .catch((err) => { if (active) setError(err); });
    return () => { active = false; };
  }, []);

  return <>
    <MainColumn>
      <div className="page-title"><h1>받은 제안</h1></div>
      <p className="page-desc">기업 회원이 내 공개 이력서를 보고 보낸 채용 제안이에요.</p>
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
        <div className="empty">아직 받은 제안이 없어요. 프로필에서 이력서를 공개하면 기업 회원이 볼 수 있어요.</div>
      ) : (
        offers.map((offer) => <OfferItem key={offer.id} offer={offer} />)
      )}
    </MainColumn>
    <RightAside label="받은 제안 안내" />
  </>;
}
