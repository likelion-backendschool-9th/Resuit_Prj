'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { ChipRadioGroup } from '@/components/common/Chip.jsx';
import { createProPayment, getMySubscription } from '@/services/companyPro.js';
import { requestTossPay } from '@/services/tossPayments.js';
import './CreditsPage.css';

// Pro 이용권. 서버(CompanyProPaymentServiceImpl)와 같은 값이다.
const PRODUCTS = [
  { id: 'PRO_1M', label: '1개월', days: 30, amount: 49000 },
  { id: 'PRO_3M', label: '3개월', days: 90, amount: 129000 }
];
const PAY_PATHS = { success: '/company/pro/success', fail: '/company/pro/fail' };

const won = (value) => `${value.toLocaleString('ko-KR')}원`;
const dateText = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '');

function planText(s) {
  if (!s?.pro) return '무료';
  if (s.trial) return `기업 Pro 체험 중 · ${dateText(s.trialEndsAt)}까지`;
  return `기업 Pro · ${dateText(s.expiresAt)}까지`;
}

export default function CompanyProPage() {
  const [subscription, setSubscription] = useState(null);
  const [selected, setSelected] = useState('PRO_1M');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    getMySubscription()
      .then((data) => { if (active) setSubscription(data); })
      .catch((err) => { if (active) setError(err); });
    return () => { active = false; };
  }, []);

  async function pay() {
    setBusy(true); setError(null);
    try {
      const order = await createProPayment(selected);
      await requestTossPay(order, PAY_PATHS);
    } catch (err) {
      // 사용자가 결제창을 닫은 경우도 여기로 온다.
      setError(err);
    } finally {
      setBusy(false);
    }
  }

  const product = PRODUCTS.find((p) => p.id === selected);
  const options = PRODUCTS.map((p) => ({ id: p.id, label: `${p.label} · ${won(p.amount)}` }));
  const blocked = error?.status === 401 || error?.status === 403;
  // 결제한 Pro가 남아 있으면 그 뒤로 이어 붙는다. 체험 중이면 체험이 끝나고 결제일부터 계산한다.
  const extendNote = subscription?.pro && !subscription.trial
    ? `지금 남은 기간(${dateText(subscription.expiresAt)}까지) 뒤로 ${product.days}일이 더해져요.`
    : subscription?.trial
      ? `결제하면 체험이 끝나고 오늘부터 ${product.days}일 동안 쓸 수 있어요.`
      : `결제하면 오늘부터 ${product.days}일 동안 쓸 수 있어요.`;

  return <>
    <MainColumn>
      <div className="page-title"><h1>기업 Pro 결제</h1></div>
      <p className="page-desc">인재 찾기의 배지·기술·경력·지역 필터를 쓰려면 기업 Pro가 필요해요.</p>
      {blocked ? (
        <div className="credits-result">
          <p className="text-2">{error.status === 401 ? '로그인한 뒤 결제할 수 있어요.' : '기업 회원만 결제할 수 있어요. 회사 이메일을 인증하면 기업 회원으로 바뀌어요.'}</p>
          <Button to={error.status === 401 ? '/login' : '/profile/verification'} variant="primary" size="sm">
            {error.status === 401 ? '로그인하기' : '기업 이메일 인증하기'}
          </Button>
        </div>
      ) : (
        <div className="credits">
          <section className="card credits__balance" aria-label="지금 요금제">
            <span className="muted small">지금 요금제</span>
            <strong>{subscription === null ? '…' : planText(subscription)}</strong>
          </section>

          <section className="credits__charge" aria-label="Pro 이용권 결제">
            <h2>이용권 고르기</h2>
            <ChipRadioGroup label="Pro 이용권" options={options} value={selected} onChange={setSelected} />
            <p className="small muted">{extendNote}</p>
            <Button variant="primary" block onClick={pay} disabled={busy || subscription === null}>
              {busy ? '결제창을 여는 중…' : `토스페이로 ${won(product.amount)} 결제하기`}
            </Button>
            <p role="alert" className="credits__error">{error?.message || ''}</p>
          </section>
        </div>
      )}
    </MainColumn>
    <RightAside label="Pro 결제 안내"><section className="card"><h2>결제 안내</h2>
      <p className="text-2">자동 갱신 없이 고른 기간만 쓰는 이용권이에요. 토스페이 테스트 결제라 실제 금액이 빠져나가지 않아요.</p>
    </section></RightAside>
  </>;
}
