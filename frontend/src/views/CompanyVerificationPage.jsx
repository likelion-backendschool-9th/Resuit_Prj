'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { cancelCompanyEmailVerification, verifyCompanyEmail } from '@/services/verifications.js';
import { getMyMentor, saveMyMentor } from '@/services/mentor.js';
import './CompanyVerificationPage.css';

const dateText = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '');

// 현직자 인증. 회사 이메일로 인증하고, 직무·경력 연차·유료 피드백 가격을 저장하면 현직자 목록에 나온다.
// 이직하면 새 회사 이메일로 다시 인증해 회사를 바꾼다. 회원 역할(일반·기업)은 바뀌지 않는다.
export default function CompanyVerificationPage() {
  const [mentor, setMentor] = useState(null);
  const [email, setEmail] = useState('');
  const [changing, setChanging] = useState(false);
  const [form, setForm] = useState({ jobTitle: '', priceCredits: '' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  function apply(data) {
    setMentor(data);
    setForm({
      jobTitle: data.jobTitle || '',
      priceCredits: data.priceCredits ?? '',
    });
  }

  useEffect(() => {
    let active = true;
    getMyMentor()
      .then((data) => { if (active) apply(data); })
      .catch((err) => { if (active) setError(err.message); });
    return () => { active = false; };
  }, []);

  // 회사 이메일 인증. 이미 인증했으면 새 회사로 바뀐다(이직).
  async function verify(event) {
    event.preventDefault();
    setBusy(true); setError(''); setNotice('');
    try {
      await verifyCompanyEmail(email.trim());
      apply(await getMyMentor());
      setEmail(''); setChanging(false);
      setNotice('회사 이메일 인증을 마쳤어요.');
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  async function save(event) {
    event.preventDefault();
    setBusy(true); setError(''); setNotice('');
    try {
      apply(await saveMyMentor({
        jobTitle: form.jobTitle.trim(),
        priceCredits: Number(form.priceCredits),
      }));
      setNotice('현직자 정보를 저장했어요. 현직자 목록에 보여요.');
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  async function cancel() {
    if (!window.confirm('현직자 인증을 취소할까요? 현직자 목록에서 빠지고 유료 피드백 요청을 받을 수 없어요.')) return;
    setBusy(true); setError(''); setNotice('');
    try {
      await cancelCompanyEmailVerification();
      apply(await getMyMentor());
      setNotice('현직자 인증을 취소했어요.');
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  const emailForm = (
    <form onSubmit={verify} className="verification__form">
      <label htmlFor="company-email">{mentor?.verified ? '새 회사 이메일' : '회사 이메일'}</label>
      <input id="company-email" type="email" autoComplete="email" maxLength={255} required
        value={email} onChange={(event) => setEmail(event.target.value)} disabled={busy} />
      <div className="verification__actions">
        <Button type="submit" disabled={busy}>{mentor?.verified ? '새 회사로 인증' : '현직자 인증'}</Button>
        {changing && <Button type="button" variant="outline" onClick={() => setChanging(false)} disabled={busy}>취소</Button>}
      </div>
    </form>
  );

  return <>
    <MainColumn>
      <div className="page-title"><h1>현직자 인증</h1></div>
      <p className="page-desc">회사 이메일로 인증하고 직무와 피드백 가격을 저장하면, 현직자로 유료 피드백 요청을 받을 수 있어요.</p>
      <div className="verification">
        {mentor === null && !error && <p className="small muted">인증 상태를 확인하는 중…</p>}
        <p role="alert" className="verification__error">{error}</p>
        {notice && <p role="status">{notice}</p>}

        {mentor && !mentor.verified && emailForm}

        {mentor?.verified && (
          <>
            <section className="verification__status" aria-label="인증 정보">
              <h2>{mentor.companyName} 현직자</h2>
              <p className="small muted">{mentor.email} · {dateText(mentor.verifiedAt)} 인증</p>
              <p className="small">{mentor.registered ? '현직자 목록에 보이고 있어요.' : '아래 정보를 저장하면 현직자 목록에 보여요.'}</p>
            </section>

            <form onSubmit={save} className="verification__form" aria-label="현직자 정보">
              <label htmlFor="mentor-job">직무</label>
              <input id="mentor-job" required maxLength={100} placeholder="예: 백엔드 개발자" disabled={busy}
                value={form.jobTitle} onChange={(event) => setForm((f) => ({ ...f, jobTitle: event.target.value }))} />
              <label htmlFor="mentor-price">유료 피드백 가격 (크레딧)</label>
              <input id="mentor-price" type="number" required min={1} max={10000} placeholder="예: 50" disabled={busy}
                value={form.priceCredits} onChange={(event) => setForm((f) => ({ ...f, priceCredits: event.target.value }))} />
              <p className="small muted">경력은 <Link href="/setting/profile" className="text-link">프로필 경력</Link>으로 계산해 현직자 목록에 보여요.</p>
              <Button type="submit" disabled={busy}>{mentor.registered ? '현직자 정보 저장' : '현직자로 등록'}</Button>
            </form>

            {changing ? emailForm : (
              <div className="verification__actions">
                <Button variant="outline" onClick={() => setChanging(true)} disabled={busy}>회사가 바뀌었어요 (다시 인증)</Button>
                <Button variant="outline" onClick={cancel} disabled={busy}>{busy ? '처리 중…' : '현직자 인증 취소'}</Button>
                <Button to="/profile" variant="outline">프로필로 돌아가기</Button>
              </div>
            )}
          </>
        )}
      </div>
    </MainColumn>
    <RightAside label="현직자 인증 안내"><section className="card"><h2>인증 안내</h2>
      <p className="text-2">등록된 회사 도메인의 이메일만 인증할 수 있어요. 회사명은 인증한 이메일 도메인으로 정해져요.</p>
      <p className="text-2">이직하면 새 회사 이메일로 다시 인증해 주세요. 직무·가격은 언제든 고칠 수 있고, 경력은 프로필 편집에서 고쳐요.</p>
    </section></RightAside>
  </>;
}
