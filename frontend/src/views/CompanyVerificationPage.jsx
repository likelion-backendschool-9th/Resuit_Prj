'use client';

import { useEffect, useState } from 'react';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import {
  confirmVerificationEmail,
  getMyVerificationStatus,
  sendVerificationEmail,
} from '@/services/verifications.js';
import './CompanyVerificationPage.css';

export default function CompanyVerificationPage() {
  const [email, setEmail] = useState('');
  const [sentEmail, setSentEmail] = useState('');
  const [code, setCode] = useState('');
  const [request, setRequest] = useState(null);
  const [cooldown, setCooldown] = useState(0);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [verified, setVerified] = useState(false);
  const [statusNote, setStatusNote] = useState('');

  useEffect(() => {
    let active = true;
    getMyVerificationStatus()
      .then((status) => {
        if (!active) return;
        if (status.status === 'VERIFIED') {
          setVerified(true);
          setSentEmail(status.email || '');
          setStatusNote('');
        } else if (status.status === 'PENDING') {
          setEmail(status.email || '');
          setSentEmail(status.email || '');
          setStatusNote('진행 중인 인증 요청이 있습니다. 메일의 코드를 입력하거나 다시 요청해 주세요.');
        } else if (status.status === 'EXPIRED') {
          setEmail(status.email || '');
          setStatusNote('이전 인증 코드가 만료되었습니다. 메일을 다시 요청해 주세요.');
        }
      })
      .catch(() => {
        if (!active) return;
        setStatusNote('');
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = setTimeout(() => setCooldown((value) => value - 1), 1000);
    return () => clearTimeout(timer);
  }, [cooldown]);

  async function send(event) {
    event.preventDefault();
    setBusy(true); setError('');
    try {
      const result = await sendVerificationEmail(email.trim());
      setRequest(result); setSentEmail(email.trim()); setCode(''); setVerified(false);
      setStatusNote('');
      setCooldown(result.resendAfterSeconds);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }
  async function confirm(event) {
    event.preventDefault();
    setBusy(true); setError('');
    try {
      const result = await confirmVerificationEmail(request.verificationId, code);
      setVerified(true);
      setSentEmail(result.email || sentEmail);
      setStatusNote('');
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  return <>
    <MainColumn>
      <div className="page-title"><h1>기업 이메일 인증</h1></div>
      <p className="page-desc">회사 이메일로 받은 인증 코드를 입력해 주세요.</p>
      <div className="verification">
        {loading && <p className="small muted">인증 상태를 확인하는 중…</p>}
        <p role="alert" className="verification__error">{error}</p>
        {!loading && statusNote && !verified && <p role="status" className="small muted">{statusNote}</p>}
        {!loading && verified ? (
          <section role="status">
            <h2>이메일 인증을 완료했어요</h2>
            <p>{sentEmail}</p>
            <Button to="/profile" variant="outline">프로필로 돌아가기</Button>
          </section>
        ) : !loading && (
          <>
            <form onSubmit={send} className="verification__form">
              <label htmlFor="company-email">기업 이메일</label>
              <input id="company-email" type="email" autoComplete="email" maxLength={255} required
                value={email} onChange={(event) => setEmail(event.target.value)} disabled={busy} />
              <Button type="submit" disabled={busy || cooldown > 0}>
                {cooldown > 0 ? `${cooldown}초 후 재발송` : request ? '인증 메일 다시 받기' : '인증 메일 받기'}
              </Button>
            </form>
            {request && <form onSubmit={confirm} className="verification__form">
              <p role="status">{sentEmail}로 인증 코드를 보냈어요. 발송 후 5분 안에 입력해 주세요.</p>
              <label htmlFor="company-code">6자리 인증 코드</label>
              <input id="company-code" inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}"
                minLength={6} maxLength={6} required value={code}
                onChange={(event) => setCode(event.target.value.replace(/[^0-9]/g, ''))} disabled={busy} />
              <Button type="submit" disabled={busy || code.length !== 6}>인증 확인</Button>
            </form>}
          </>
        )}
      </div>
    </MainColumn>
    <RightAside label="기업 이메일 인증 안내"><section className="card"><h2>인증 안내</h2>
      <p className="text-2">수신 가능한 이메일을 입력해 주세요. 메일이 보이지 않으면 스팸함도 확인해 주세요.</p>
    </section></RightAside>
  </>;
}
