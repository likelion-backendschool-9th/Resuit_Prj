'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Avatar from '@/components/common/Avatar.jsx';
import Button from '@/components/common/Button.jsx';
import { avatarColor } from '@/components/feedback/labels.js';
import { dateText } from '@/components/paidFeedback/labels.js';
import { useAppState } from '@/context/AppStateContext.jsx';
import { createPaidFeedback, getMentor } from '@/services/paidFeedback.js';
import { getAllResumes } from '@/services/resumes.js';
import { getCreditBalance } from '@/services/credits.js';
import '@/components/paidFeedback/PaidFeedback.css';

// 고를 이력서 값은 "COMMON:3"처럼 종류와 ID를 붙여 쓴다.
const resumeOptions = (data) => [
  ...(data?.commonResumes || []).map((r) => ({ value: `COMMON:${r.id}`, label: `공통 이력서 · ${r.title}` })),
  ...(data?.targetResumes || []).map((r) => ({ value: `TARGET:${r.id}`, label: `특화 이력서 · ${[r.companyName, r.jobTitle].filter(Boolean).join(' ')}` }))
];

// 현직자 상세(8.2)와 유료 피드백 요청(8.3). 요청하면 가격만큼 크레딧이 빠진다.
export default function MentorDetailPage({ id }) {
  const router = useRouter();
  const { me } = useAppState();
  const [mentor, setMentor] = useState(null);
  const [error, setError] = useState(null);
  const [options, setOptions] = useState([]);
  const [balance, setBalance] = useState(null);
  const [resume, setResume] = useState('');
  const [content, setContent] = useState('');
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => {
    let active = true;
    getMentor(id).then((data) => { if (active) setMentor(data); }).catch((err) => { if (active) setError(err); });
    getAllResumes().then((data) => {
      if (!active) return;
      const list = resumeOptions(data);
      setOptions(list);
      setResume((value) => value || list[0]?.value || '');
    }).catch(() => {});
    getCreditBalance().then((data) => { if (active) setBalance(data.balance); }).catch(() => {});
    return () => { active = false; };
  }, [id]);

  async function submit(event) {
    event.preventDefault();
    if (!resume) { setFormError('피드백 받을 이력서를 골라 주세요.'); return; }
    const [type, resumeId] = resume.split(':');
    setBusy(true); setFormError('');
    try {
      const created = await createPaidFeedback({
        mentorId: id,
        commonResumeId: type === 'COMMON' ? Number(resumeId) : null,
        targetResumeId: type === 'TARGET' ? Number(resumeId) : null,
        content: content.trim() || null
      });
      router.push(`/paid-feedback/${created.id}`);
    } catch (err) {
      setFormError(err.message);
      setBusy(false);
    }
  }

  if (error) return <>
    <MainColumn><div className="page-title"><h1>현직자 상세</h1></div><div className="empty" role="alert">{error.message}</div></MainColumn>
    <RightAside label="현직자 안내" />
  </>;
  if (!mentor) return <>
    <MainColumn><div className="page-title"><h1>현직자 상세</h1></div><div className="empty">불러오는 중…</div></MainColumn>
    <RightAside label="현직자 안내" />
  </>;

  const name = mentor.nickname || mentor.username;
  const history = mentor.reviewHistory;
  const isMe = me?.handle === mentor.username;
  const enough = balance === null || balance >= mentor.priceCredits;

  return <>
    <MainColumn>
      <div className="page-title"><h1>현직자 상세</h1></div>
      <div className="paid-page">
        <section className="mentor-card" style={{ padding: 0, borderBottom: 0 }}>
          <Avatar initial={name.slice(0, 1)} color={avatarColor(mentor.username)} src={mentor.profileImageUrl} size={64} />
          <div className="mentor-card__body">
            <div className="row-wrap" style={{ gap: 6 }}><strong style={{ fontSize: 20 }}>{name}</strong><span className="small muted">@{mentor.username}</span></div>
            <div className="row-wrap" style={{ gap: 8 }}>
              <span className="mentor-badge">✓ {mentor.companyName} 현직자</span>
              <span className="small">{mentor.jobTitle}{mentor.careerYears != null ? ` · 경력 ${mentor.careerYears}년` : ''}</span>
            </div>
            <span className="xs muted">{dateText(mentor.verifiedAt)} 인증</span>
          </div>
        </section>
        {mentor.selfIntroduction && <p className="paid-quote">{mentor.selfIntroduction}</p>}
        <section style={{ display: 'grid', gap: 6 }}>
          <h2>피드백 이력</h2>
          <p className="small">유료 피드백 완료 {history.completedPaidFeedbackCount}건 · 공개 피드백 댓글 {history.publicReviewCount}개 (채택 {history.adoptedReviewCount})</p>
        </section>

        <section id="request" style={{ display: 'grid', gap: 12 }}>
          <h2>피드백 요청하기</h2>
          {isMe ? <p className="small muted">내 현직자 정보예요. 자기 자신에게는 요청할 수 없어요.</p> : (
            <form className="paid-form" onSubmit={submit}>
              <label htmlFor="paid-resume">피드백 받을 이력서</label>
              {options.length === 0 ? (
                <p className="small muted">요청할 이력서가 없어요. <Link href="/resumes">내 이력서</Link>에서 먼저 만들어 주세요.</p>
              ) : (
                <select id="paid-resume" value={resume} onChange={(event) => setResume(event.target.value)} disabled={busy}>
                  {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
              )}
              <label htmlFor="paid-content">요청 내용 (선택)</label>
              <textarea id="paid-content" rows={4} maxLength={2000} value={content} disabled={busy}
                onChange={(event) => setContent(event.target.value)} placeholder="특히 봐 줬으면 하는 부분을 적어 주세요." />
              <div className="paid-cost">
                가격 <strong>{mentor.priceCredits.toLocaleString('ko-KR')} 크레딧</strong> · 요청하면 바로 차감돼요
                {balance !== null && ` (보유 ${balance.toLocaleString('ko-KR')} → ${(balance - mentor.priceCredits).toLocaleString('ko-KR')})`}
              </div>
              {!enough && <p className="small paid-error">크레딧이 부족해요. <Link href="/credits">크레딧 충전</Link> 후 요청해 주세요.</p>}
              {formError && <p role="alert" className="small paid-error">{formError}</p>}
              <Button type="submit" block disabled={busy || options.length === 0 || !enough}>
                {busy ? '요청하는 중…' : `${mentor.priceCredits.toLocaleString('ko-KR')} 크레딧으로 요청하기`}
              </Button>
            </form>
          )}
        </section>
      </div>
    </MainColumn>
    <RightAside label="내 크레딧">
      <section className="card" style={{ gap: 8 }}>
        <h2>내 크레딧</h2>
        <strong style={{ fontSize: 20, color: 'var(--accent-text)' }}>{balance === null ? '…' : `${balance.toLocaleString('ko-KR')} 크레딧`}</strong>
        <Button to="/credits" variant="outline" size="sm">충전하기</Button>
      </section>
    </RightAside>
  </>;
}
