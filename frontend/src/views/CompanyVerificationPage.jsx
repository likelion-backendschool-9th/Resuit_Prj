'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { useAppState } from '@/context/AppStateContext.jsx';
import { cancelCompanyEmailVerification, getMyVerificationStatus, verifyCompanyEmail } from '@/services/verifications.js';
import './CompanyVerificationPage.css';

export default function CompanyVerificationPage() {
  const { refreshSession } = useAppState();
  const [email, setEmail] = useState('');
  const [verifiedEmail, setVerifiedEmail] = useState('');
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [verified, setVerified] = useState(false);

  useEffect(() => {
    let active = true;
    getMyVerificationStatus()
      .then((status) => {
        if (!active) return;
        if (status.status === 'VERIFIED') {
          setVerified(true);
          setVerifiedEmail(status.email || '');
        }
      })
      .catch(() => {})
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  async function submit(event) {
    event.preventDefault();
    setBusy(true); setError(''); setNotice('');
    try {
      const result = await verifyCompanyEmail(email.trim());
      setVerified(true);
      setVerifiedEmail(result.email || email.trim());
      await refreshSession?.().catch(() => {});
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  async function cancel() {
    if (!window.confirm('기업 이메일 인증을 취소할까요? 기업 회원 권한이 해제됩니다.')) return;
    setBusy(true); setError(''); setNotice('');
    try {
      await cancelCompanyEmailVerification();
      setVerified(false);
      setVerifiedEmail('');
      setEmail('');
      setNotice('기업 이메일 인증이 취소됐어요.');
      await refreshSession?.().catch(() => {});
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  return <>
    <MainColumn>
      <div className="page-title"><h1>기업 이메일 인증</h1></div>
      <p className="page-desc">회사 이메일을 입력하면 기업 회원으로 전환돼요.</p>
      <div className="verification">
        {loading && <p className="small muted">인증 상태를 확인하는 중…</p>}
        <p role="alert" className="verification__error">{error}</p>
        {notice && <p role="status">{notice}</p>}
        {!loading && verified ? (
          <section role="status">
            <h2>기업 회원으로 전환됐어요</h2>
            <p>{verifiedEmail}</p>
            <div className="verification__actions">
              <Button onClick={cancel} disabled={busy} variant="outline">
                {busy ? '인증 취소 중…' : '기업 이메일 인증 취소'}
              </Button>
              <Button to="/profile" variant="outline">프로필로 돌아가기</Button>
            </div>
          </section>
        ) : !loading && (
          <form onSubmit={submit} className="verification__form">
            <label htmlFor="company-email">기업 이메일</label>
            <input id="company-email" type="email" autoComplete="email" maxLength={255} required
              value={email} onChange={(event) => setEmail(event.target.value)} disabled={busy} />
            <Button type="submit" disabled={busy}>기업 회원 인증</Button>
          </form>
        )}
      </div>
    </MainColumn>
    <RightAside label="기업 이메일 인증 안내"><section className="card"><h2>인증 안내</h2>
      <p className="text-2">등록된 회사 도메인의 이메일만 인증할 수 있어요.</p>
    </section></RightAside>
  </>;
}
