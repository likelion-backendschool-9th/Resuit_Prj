import { useState } from 'react';
import Button from '../common/Button.jsx';
import './CreditWithdrawForm.css';

// 환전할 때 1크레딧당 받는 금액(원). 서버(CreditServiceImpl)와 같은 값이다. 충전가 100원에서 수수료 20%를 뺐다.
const KRW_PER_CREDIT = 80;
const FEE_PER_CREDIT = 20;
const BANKS = ['KB국민은행', '신한은행', '우리은행', '하나은행', 'NH농협은행', 'IBK기업은행', '카카오뱅크', '토스뱅크', '케이뱅크'];
const EMPTY = { credits: '', bankName: BANKS[0], accountNumber: '', accountHolder: '' };

const won = (value) => `${value.toLocaleString('ko-KR')}원`;

// 적립 크레딧 환전 폼. 환전 요청은 onWithdraw로 받아서 API 호출 방식을 모른다.
export default function CreditWithdrawForm({ withdrawable, onWithdraw }) {
  const [form, setForm] = useState(EMPTY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState(null);

  const credits = Number(form.credits) || 0;
  const disabled = !withdrawable;
  const set = (key) => (event) => setForm((f) => ({ ...f, [key]: event.target.value }));

  async function submit(event) {
    event.preventDefault();
    setError(''); setDone(null);
    if (credits < 1 || credits > withdrawable) {
      setError(`1 ~ ${withdrawable.toLocaleString('ko-KR')} 크레딧 사이로 입력해 주세요.`);
      return;
    }
    setBusy(true);
    try {
      const result = await onWithdraw({ ...form, credits });
      setDone(result);
      setForm((f) => ({ ...EMPTY, bankName: f.bankName, accountNumber: f.accountNumber, accountHolder: f.accountHolder }));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="credit-withdraw" onSubmit={submit} aria-label="크레딧 환전">
      <p className="small muted">
        피드백으로 적립한 크레딧만 환전돼요. 지금 <strong className="credit-withdraw__max">{withdrawable.toLocaleString('ko-KR')} 크레딧</strong>을 환전할 수 있어요.
      </p>

      <div className="field">
        <label htmlFor="withdraw-credits">환전할 크레딧</label>
        <div className="credit-withdraw__amount">
          <input id="withdraw-credits" className="input" type="number" min={1} max={withdrawable || undefined} inputMode="numeric"
            placeholder="예: 100" disabled={disabled || busy} value={form.credits} onChange={set('credits')} />
          <Button type="button" variant="outline" size="mid" disabled={disabled || busy}
            onClick={() => setForm((f) => ({ ...f, credits: String(withdrawable) }))}>전액</Button>
        </div>
      </div>

      <div className="credit-withdraw__account">
        <div className="field">
          <label htmlFor="withdraw-bank">은행</label>
          <select id="withdraw-bank" className="input" disabled={disabled || busy} value={form.bankName} onChange={set('bankName')}>
            {BANKS.map((bank) => <option key={bank} value={bank}>{bank}</option>)}
          </select>
        </div>
        <div className="field">
          <label htmlFor="withdraw-account">계좌번호</label>
          <input id="withdraw-account" className="input" required maxLength={30} inputMode="numeric" placeholder="- 없이 숫자만"
            disabled={disabled || busy} value={form.accountNumber} onChange={set('accountNumber')} />
        </div>
        <div className="field">
          <label htmlFor="withdraw-holder">예금주</label>
          <input id="withdraw-holder" className="input" required maxLength={50} placeholder="예: 홍길동"
            disabled={disabled || busy} value={form.accountHolder} onChange={set('accountHolder')} />
        </div>
      </div>

      <dl className="credit-withdraw__summary">
        <div><dt>환전 크레딧</dt><dd>{credits.toLocaleString('ko-KR')} 크레딧</dd></div>
        <div><dt>수수료 (20%)</dt><dd>−{won(credits * FEE_PER_CREDIT)}</dd></div>
        <div className="is-total"><dt>받는 금액</dt><dd>{won(credits * KRW_PER_CREDIT)}</dd></div>
      </dl>

      <Button type="submit" variant="primary" block disabled={disabled || busy}>
        {busy ? '환전하는 중…' : disabled ? '환전할 수 있는 크레딧이 없어요' : `${won(credits * KRW_PER_CREDIT)} 환전하기`}
      </Button>
      {error && <p role="alert" className="credit-withdraw__error">{error}</p>}
      {done && (
        <p role="status" className="credit-withdraw__done">
          {done.bankName} {done.accountNumber} ({done.accountHolder}) 계좌로 {won(done.amountKrw)}을 보냈어요.
        </p>
      )}
    </form>
  );
}
