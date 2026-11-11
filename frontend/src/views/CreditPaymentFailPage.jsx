'use client';

import { useSearchParams } from 'next/navigation';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import './CreditsPage.css';

// 토스 결제창에서 실패하거나 취소하면 이 화면으로 온다(?code=&message=&orderId=). 크레딧은 충전되지 않는다.
export default function CreditPaymentFailPage() {
  const params = useSearchParams();
  const message = params.get('message') || '결제가 완료되지 않았어요.';

  return <>
    <MainColumn>
      <div className="page-title"><h1>크레딧 충전</h1></div>
      <div className="credits-result">
        <h2>결제가 완료되지 않았어요</h2>
        <p role="alert" className="credits-result__error">{message}</p>
        <Button to="/credits" variant="outline">다시 충전하기</Button>
      </div>
    </MainColumn>
    <RightAside label="크레딧 안내" />
  </>;
}
