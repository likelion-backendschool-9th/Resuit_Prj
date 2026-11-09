'use client';

import { useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { confirmCreditPayment } from '@/services/credits.js';
import './CreditsPage.css';

// 토스 결제창이 성공하면 이 화면으로 온다(?paymentKey=&orderId=&amount=). 서버에 승인을 요청해야 충전된다.
export default function CreditPaymentSuccessPage() {
  const params = useSearchParams();
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  // 개발 모드에서 useEffect가 두 번 실행돼도 승인은 한 번만 요청한다(서버도 같은 결제는 한 번만 충전한다).
  const requested = useRef(false);

  useEffect(() => {
    if (requested.current) return;
    requested.current = true;
    const paymentKey = params.get('paymentKey');
    const orderId = params.get('orderId');
    const amount = params.get('amount');
    if (!paymentKey || !orderId || !amount) {
      setError('결제 정보가 올바르지 않아요.');
      return;
    }
    confirmCreditPayment({ paymentKey, orderId, amount })
      .then(setResult)
      .catch((err) => setError(err.message));
  }, [params]);

  return <>
    <MainColumn>
      <div className="page-title"><h1>크레딧 충전</h1></div>
      <div className="credits-result">
        {!result && !error && <p className="muted">결제를 확인하는 중…</p>}
        {result && (
          <section role="status">
            <h2>{result.credits} 크레딧을 충전했어요</h2>
            <p className="text-2">보유 크레딧 {result.balance.toLocaleString('ko-KR')}</p>
          </section>
        )}
        {error && <p role="alert" className="credits-result__error">{error}</p>}
        <Button to="/credits" variant="outline">크레딧으로 돌아가기</Button>
      </div>
    </MainColumn>
    <RightAside label="크레딧 안내" />
  </>;
}
