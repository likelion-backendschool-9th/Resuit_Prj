'use client';

import { useSearchParams } from 'next/navigation';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import './CreditsPage.css';

// 토스 결제창에서 실패하거나 취소하면 이 화면으로 온다(?code=&message=&orderId=). Pro 기간은 바뀌지 않는다.
export default function ProPaymentFailPage() {
  const params = useSearchParams();
  const message = params.get('message') || '결제가 완료되지 않았어요.';

  return <>
    <MainColumn>
      <div className="page-title"><h1>기업 Pro 결제</h1></div>
      <div className="credits-result">
        <h2>결제가 완료되지 않았어요</h2>
        <p role="alert" className="credits-result__error">{message}</p>
        <Button to="/company/pro" variant="outline">다시 결제하기</Button>
      </div>
    </MainColumn>
    <RightAside label="Pro 결제 안내" />
  </>;
}
