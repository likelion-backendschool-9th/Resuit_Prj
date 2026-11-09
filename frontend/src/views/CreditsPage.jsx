'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { ChipRadioGroup } from '@/components/common/Chip.jsx';
import { createCreditPayment, getCreditBalance, getCreditTransactions } from '@/services/credits.js';
import { requestTossPay } from '@/services/tossPayments.js';
import './CreditsPage.css';

// 1크레딧 가격(원). 서버(CreditPaymentServiceImpl)와 같은 값이다.
const KRW_PER_CREDIT = 100;
const PACKAGES = [100, 300, 500, 1000];
const TYPE_LABEL = { CHARGE: '충전', USE: '사용', EARN: '적립' };

const won = (value) => `${value.toLocaleString('ko-KR')}원`;

export default function CreditsPage() {
  const [balance, setBalance] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [selected, setSelected] = useState('300');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    Promise.all([getCreditBalance(), getCreditTransactions()])
      .then(([balanceResult, page]) => {
        if (!active) return;
        setBalance(balanceResult.balance);
        setTransactions(page.content || []);
      })
      .catch((err) => { if (active) setError(err.message); });
    return () => { active = false; };
  }, []);

  async function charge() {
    setBusy(true); setError('');
    try {
      const order = await createCreditPayment(Number(selected));
      await requestTossPay(order);
    } catch (err) {
      // 사용자가 결제창을 닫은 경우도 여기로 온다.
      setError(err.message || '결제를 시작하지 못했어요.');
    } finally {
      setBusy(false);
    }
  }

  const options = PACKAGES.map((credits) => ({ id: String(credits), label: `${credits} 크레딧 · ${won(credits * KRW_PER_CREDIT)}` }));

  return <>
    <MainColumn>
      <div className="page-title"><h1>크레딧</h1></div>
      <p className="page-desc">유료 피드백에 쓰는 크레딧을 충전해요.</p>
      <div className="credits">
        <section className="card credits__balance" aria-label="보유 크레딧">
          <span className="muted small">보유 크레딧</span>
          <strong>{balance === null ? '…' : `${balance.toLocaleString('ko-KR')} 크레딧`}</strong>
        </section>

        <section className="credits__charge" aria-label="크레딧 충전">
          <h2>충전하기</h2>
          <ChipRadioGroup label="충전할 크레딧" options={options} value={selected} onChange={setSelected} />
          <Button variant="primary" block onClick={charge} disabled={busy}>
            {busy ? '결제창을 여는 중…' : `토스페이로 ${won(Number(selected) * KRW_PER_CREDIT)} 결제하기`}
          </Button>
          <p role="alert" className="credits__error">{error}</p>
        </section>

        <section aria-label="최근 크레딧 내역">
          <h2>최근 내역</h2>
          {transactions.length === 0 ? (
            <p className="muted small">아직 크레딧 내역이 없어요.</p>
          ) : (
            <ul className="credits__list">
              {transactions.map((t) => (
                <li key={t.id} className="credits__item">
                  <span>{TYPE_LABEL[t.type] || t.type}</span>
                  <span className={`credits__amount${t.amount > 0 ? ' is-plus' : ''}`}>{t.amount > 0 ? `+${t.amount}` : t.amount}</span>
                  <span className="muted small">잔액 {t.balanceAfter} · {t.createdAt?.slice(0, 10)}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </MainColumn>
    <RightAside label="크레딧 안내"><section className="card"><h2>충전 안내</h2>
      <p className="text-2">1크레딧은 100원이에요. 토스페이 테스트 결제라 실제 금액이 빠져나가지 않아요.</p>
    </section></RightAside>
  </>;
}
