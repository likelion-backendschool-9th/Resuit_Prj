'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import { ChipRadioGroup } from '@/components/common/Chip.jsx';
import { JOB_OPTIONS, jobLabel } from '@/components/paidFeedback/labels.js';
import { cancelCompanyEmailVerification, verifyCompanyEmail } from '@/services/verifications.js';
import { getMyMentor, saveMyMentor } from '@/services/mentor.js';
import './CompanyVerificationPage.css';

const dateText = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '');

function emailDomain(value) {
  const at = value.lastIndexOf('@');
  if (at < 0) return '';
  return value.slice(at + 1).trim().toLowerCase();
}

function EmailComposer({ verified, email, setEmail, busy, changing, onCancelChange, onSubmit }) {
  const domain = emailDomain(email);
  const local = domain ? email.slice(0, email.lastIndexOf('@')) : email;

  return (
    <form onSubmit={onSubmit} className="verify-mail">
      <div className="verify-mail__editor">
        <div className="verify-mail__meta">
          <span>From</span>
          <strong>Resuit 현직자 인증</strong>
        </div>
        <div className="verify-mail__meta">
          <span>Subject</span>
          <strong>{verified ? '회사 변경 인증 요청' : '현직자 자격 확인 요청'}</strong>
        </div>

        <label htmlFor="company-email" className="verify-mail__to-label">회사 이메일</label>
        <div className={`verify-mail__to${domain ? ' is-ready' : ''}`}>
          <span aria-hidden="true">To</span>
          <input
            id="company-email"
            type="email"
            autoComplete="email"
            maxLength={255}
            required
            placeholder="name@company.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            disabled={busy}
          />
        </div>

        <div className="verify-mail__body" aria-live="polite">
          {domain ? (
            <>
              <p className="verify-mail__body-title">이 메일로 인증합니다</p>
              <p className="verify-mail__address">{local || '…'}@{domain}</p>
              <p>
                회사 도메인 <strong>{domain}</strong> 이 등록되어 있으면
                코드 확인 없이 바로 현직자 인증이 끝나요.
              </p>
            </>
          ) : (
            <>
              <p className="verify-mail__body-title">재직 중인 회사 메일을 적어 주세요</p>
              <p>
                예: <code>kim@newlecture.com</code>
                <br />
                → 회사 도메인 <strong>newlecture.com</strong>
              </p>
            </>
          )}
        </div>
      </div>

      <div className="verify-actions">
        <Button type="submit" disabled={busy || !domain} size="lg">
          {busy ? '인증하는 중…' : verified ? '새 회사로 인증하기' : '현직자 인증 시작하기'}
        </Button>
        {changing && (
          <Button type="button" variant="outline" onClick={onCancelChange} disabled={busy}>취소</Button>
        )}
      </div>
    </form>
  );
}

function MentorForm({ form, setForm, busy, registered, onSubmit, idPrefix = 'mentor' }) {
  return (
    <form onSubmit={onSubmit} className="verify-setup-form" aria-label="현직자 정보">
      <div className="verify-setup-form__grid">
        <div>
          <span id={`${idPrefix}-job`} className="verify-setup-form__label">직무 (필수)</span>
          <fieldset disabled={busy} aria-labelledby={`${idPrefix}-job`} className="verify-setup-form__jobs">
            <ChipRadioGroup options={JOB_OPTIONS} value={form.jobTitle} onChange={(value) => setForm((f) => ({ ...f, jobTitle: value }))} label="직무" />
          </fieldset>
        </div>
        <div>
          <label htmlFor={`${idPrefix}-price`}>피드백 가격 (🪙)</label>
          <input
            id={`${idPrefix}-price`}
            type="number"
            required
            min={1}
            max={10000}
            placeholder="예: 50"
            disabled={busy}
            value={form.priceCredits}
            onChange={(event) => setForm((f) => ({ ...f, priceCredits: event.target.value }))}
          />
        </div>
      </div>
      <p className="verify-hint">
        경력은 <Link href="/setting/profile" className="text-link">프로필 경력</Link>으로 계산돼요.
      </p>
      <div className="verify-actions">
        <Button type="submit" disabled={busy} size="lg">
          {busy ? '저장 중…' : registered ? '정보 저장' : '현직자 목록에 공개하기'}
        </Button>
      </div>
    </form>
  );
}

// 현직자 인증. 회사 이메일로 인증하고 직무·가격을 저장하면 현직자 목록에 나온다.
export default function CompanyVerificationPage() {
  const [mentor, setMentor] = useState(null);
  const [email, setEmail] = useState('');
  const [changing, setChanging] = useState(false);
  const [form, setForm] = useState({ jobTitle: '', priceCredits: '' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  // 서버 응답을 기다리다 화면이 비지 않도록 시작 화면을 먼저 보여 준다.
  const [phase, setPhase] = useState('start'); // start | success-email | setup | success-live | live
  const [checking, setChecking] = useState(true);

  function apply(data, nextPhase) {
    setMentor(data);
    setForm({
      // 직접 입력하던 때의 값이면 비워 두어 다시 고르게 한다.
      jobTitle: JOB_OPTIONS.some((o) => o.id === data.jobTitle) ? data.jobTitle : '',
      priceCredits: data.priceCredits ?? '',
    });
    if (nextPhase) setPhase(nextPhase);
    else if (data.registered) setPhase('live');
    else if (data.verified) setPhase('setup');
    else setPhase('start');
  }

  useEffect(() => {
    let active = true;
    const timeout = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('서버에 연결하지 못했어요. 백엔드 실행 후 다시 시도해 주세요.')), 3500);
    });

    Promise.race([getMyMentor(), timeout])
      .then((data) => { if (active) apply(data); })
      .catch((err) => {
        if (!active) return;
        setError(err.message);
        setPhase((current) => (current === 'start' ? current : 'start'));
      })
      .finally(() => { if (active) setChecking(false); });

    return () => { active = false; };
  }, []);

  async function verify(event) {
    event.preventDefault();
    setBusy(true); setError('');
    try {
      await verifyCompanyEmail(email.trim());
      const data = await getMyMentor();
      setEmail(''); setChanging(false);
      apply(data, data.registered ? 'success-live' : 'success-email');
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  async function save(event) {
    event.preventDefault();
    if (!form.jobTitle) {
      setError('직무를 골라 주세요.');
      return;
    }
    setBusy(true); setError('');
    const wasRegistered = !!mentor?.registered;
    try {
      const data = await saveMyMentor({
        jobTitle: form.jobTitle,
        priceCredits: Number(form.priceCredits),
      });
      apply(data, wasRegistered ? 'live' : 'success-live');
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  async function cancel() {
    if (!window.confirm('현직자 인증을 취소할까요? 현직자 목록에서 빠지고 유료 피드백 요청을 받을 수 없어요.')) return;
    setBusy(true); setError('');
    try {
      await cancelCompanyEmailVerification();
      apply(await getMyMentor(), 'start');
      setChanging(false);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  const company = mentor?.companyName || '회사';

  return (
    <>
      <MainColumn>
          <div className={`verify-shell verify-shell--${phase}`}>
            {checking && <p className="verify-checking" aria-live="polite">인증 상태를 확인하는 중…</p>}
            {error && <p role="alert" className="verify-alert">{error}</p>}

            {/* 1) 시작: 메일 작성만 크게 */}
            {phase === 'start' && (
              <section className="verify-start">
                <div className="verify-start__intro">
                  <p className="verify-kicker">현직자 인증</p>
                  <h1>회사 메일 한 통으로<br />현직자 인증을 시작해요</h1>
                  <p className="verify-lead">
                    등록된 회사 도메인 이메일이면 바로 인증돼요.
                    그다음 직무와 가격만 정하면 목록에 공개됩니다.
                  </p>
                </div>
                <EmailComposer
                  verified={false}
                  email={email}
                  setEmail={setEmail}
                  busy={busy || checking}
                  changing={false}
                  onSubmit={verify}
                />
              </section>
            )}

            {/* 2) 이메일 인증 직후: 완전 다른 축하 화면 */}
            {phase === 'success-email' && (
              <section className="verify-success verify-success--email" role="status" aria-live="polite">
                <div className="verify-success__mark" aria-hidden="true">
                  <svg viewBox="0 0 72 72" width="72" height="72" fill="none">
                    <circle cx="36" cy="36" r="32" stroke="currentColor" strokeWidth="3" />
                    <path d="M22 37.5 32 47.5 51 26" stroke="currentColor" strokeWidth="4.5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </div>
                <p className="verify-kicker">이메일 인증 완료</p>
                <h1>{company}<br />현직자로 확인됐어요</h1>
                <p className="verify-lead">
                  {mentor?.email} · {dateText(mentor?.verifiedAt)} 인증
                </p>
                <div className="verify-success__card">
                  <span>다음 단계</span>
                  <strong>직무와 피드백 가격을 정하면 현직자 목록에 공개돼요.</strong>
                </div>
                <div className="verify-actions">
                  <Button type="button" size="lg" onClick={() => setPhase('setup')}>
                    이어서 정보 등록하기
                  </Button>
                </div>
              </section>
            )}

            {/* 3) 정보 등록 단계 */}
            {phase === 'setup' && (
              <section className="verify-setup">
                <div className="verify-setup__pass">
                  <p className="verify-kicker">인증 완료</p>
                  <h1>{company}</h1>
                  <p>{mentor?.email}</p>
                  <span className="verify-setup__chip">이메일 인증됨</span>
                </div>
                <div className="verify-setup__panel">
                  <p className="verify-kicker">Step 2</p>
                  <h2>현직자 정보 등록</h2>
                  <p className="verify-lead">목록에 보여줄 직무와 유료 피드백 가격을 정해요.</p>
                  <MentorForm
                    form={form}
                    setForm={setForm}
                    busy={busy}
                    registered={false}
                    onSubmit={save}
                  />
                </div>
              </section>
            )}

            {/* 4) 목록 공개 직후: 또 다른 축하 화면 */}
            {phase === 'success-live' && (
              <section className="verify-success verify-success--live" role="status" aria-live="polite">
                <div className="verify-success__banner" aria-hidden="true">LIVE</div>
                <p className="verify-kicker">현직자 등록 완료</p>
                <h1>이제 현직자 목록에<br />공개됐어요</h1>
                <p className="verify-lead">
                  {company} · {jobLabel(form.jobTitle || mentor?.jobTitle) || '직무'} ·{' '}
                  {Number(form.priceCredits || mentor?.priceCredits || 0).toLocaleString('ko-KR')} 🪙
                </p>
                <div className="verify-actions">
                  <Button to="/mentors" size="lg">현직자 목록 확인</Button>
                  <Button type="button" variant="outline" size="lg" onClick={() => setPhase('live')}>
                    내 인증 관리
                  </Button>
                </div>
              </section>
            )}

            {/* 5) 관리 화면: 시작/축하와 전혀 다른 대시보드 */}
            {phase === 'live' && (
              <section className="verify-live">
                <div className="verify-live__hero">
                  <div>
                    <p className="verify-kicker">활동 중</p>
                    <h1>{company} 현직자</h1>
                    <p className="verify-lead">현직자 목록에 공개되어 피드백 요청을 받을 수 있어요.</p>
                  </div>
                  <div className="verify-live__stats">
                    <div>
                      <span>직무</span>
                      <strong>{jobLabel(mentor?.jobTitle) || '-'}</strong>
                    </div>
                    <div>
                      <span>가격</span>
                      <strong>{Number(mentor?.priceCredits || 0).toLocaleString('ko-KR')} 🪙</strong>
                    </div>
                    <div>
                      <span>인증일</span>
                      <strong>{dateText(mentor?.verifiedAt) || '-'}</strong>
                    </div>
                  </div>
                </div>

                <div className="verify-live__grid">
                  <div className="verify-live__panel">
                    <h2>정보 수정</h2>
                    <MentorForm
                      form={form}
                      setForm={setForm}
                      busy={busy}
                      registered
                      onSubmit={save}
                      idPrefix="live"
                    />
                  </div>
                  <div className="verify-live__panel verify-live__panel--manage">
                    <h2>인증 관리</h2>
                    <p className="verify-lead">이직했거나 활동을 끝낼 때 사용해요.</p>
                    {changing ? (
                      <EmailComposer
                        verified
                        email={email}
                        setEmail={setEmail}
                        busy={busy}
                        changing
                        onCancelChange={() => setChanging(false)}
                        onSubmit={verify}
                      />
                    ) : (
                      <div className="verify-actions">
                        <Button variant="outline" onClick={() => setChanging(true)} disabled={busy}>
                          회사가 바뀌었어요
                        </Button>
                        <Button variant="outline" onClick={cancel} disabled={busy}>
                          {busy ? '처리 중…' : '현직자 인증 취소'}
                        </Button>
                        <Button to="/mentors" variant="outline">현직자 목록</Button>
                      </div>
                    )}
                  </div>
                </div>
              </section>
            )}
          </div>
      </MainColumn>

      <RightAside label="현직자 인증 안내">
        <section className="card verify-aside">
          <h2>지금 단계</h2>
          <ol className="verify-aside__steps">
            <li className={phase === 'start' ? 'is-on' : (phase !== 'loading' ? 'is-done' : '')}>회사 이메일 인증</li>
            <li className={phase === 'setup' || phase === 'success-email' ? 'is-on' : (phase === 'live' || phase === 'success-live' ? 'is-done' : '')}>직무 · 가격 등록</li>
            <li className={phase === 'live' || phase === 'success-live' ? 'is-on is-done' : ''}>목록 공개</li>
          </ol>
        </section>
        <section className="card verify-aside">
          <h2>알아두기</h2>
          <p className="text-2">회사명은 인증한 이메일 도메인으로 정해져요.</p>
          <p className="text-2">이직하면 새 회사 메일로 다시 인증하세요.</p>
          <p className="text-2">경력은 프로필 편집에서 관리해요.</p>
        </section>
      </RightAside>
    </>
  );
}
