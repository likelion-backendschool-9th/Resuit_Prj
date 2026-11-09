'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Badge from '@/components/common/Badge.jsx';
import Button from '@/components/common/Button.jsx';
import ResumePreview from '@/components/feedback/ResumePreview.jsx';
import { dateText, statusOf } from '@/components/paidFeedback/labels.js';
import { useAppState } from '@/context/AppStateContext.jsx';
import { answerPaidFeedback, getPaidFeedback, getPaidFeedbackResume } from '@/services/paidFeedback.js';
import '@/components/paidFeedback/PaidFeedback.css';

// 유료 피드백 상세(8.5)와 현직자 답변(8.6). 요청자와 그 요청의 현직자만 볼 수 있다.
export default function PaidFeedbackDetailPage({ id }) {
  const { me } = useAppState();
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState(null);
  const [answer, setAnswer] = useState('');
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => {
    let active = true;
    getPaidFeedback(id).then((data) => { if (active) setDetail(data); }).catch((err) => { if (active) setError(err); });
    return () => { active = false; };
  }, [id]);

  // 요청에 붙은 이력서는 유료 피드백 전용 API로 읽는다(일반 미리보기로는 볼 수 없다).
  const loadResume = useCallback((signal) => getPaidFeedbackResume(id, signal), [id]);

  async function submit(event) {
    event.preventDefault();
    if (!answer.trim()) { setFormError('답변 내용을 입력해 주세요.'); return; }
    setBusy(true); setFormError('');
    try {
      await answerPaidFeedback(id, answer.trim());
      setAnswer('');
      setDetail(await getPaidFeedback(id));
    } catch (err) {
      setFormError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const title = <div className="page-title"><h1>유료 피드백 요청</h1><Button to="/paid-feedback" variant="outline" size="sm">목록으로</Button></div>;
  if (error) return <><MainColumn>{title}<div className="empty" role="alert">{error.message}</div></MainColumn><RightAside label="답변 안내" /></>;
  if (!detail) return <><MainColumn>{title}<div className="empty">불러오는 중…</div></MainColumn><RightAside label="답변 안내" /></>;

  const { summary } = detail;
  const status = statusOf(summary.status);
  const amMentor = summary.mentor.username === me?.handle;
  const requester = summary.requester.nickname || summary.requester.username;
  const mentor = summary.mentor.nickname || summary.mentor.username;
  const canAnswer = amMentor && summary.status !== 'CANCELED';
  const type = detail.commonResumeId ? 'COMMON' : 'TARGET';
  const resumeId = detail.commonResumeId || detail.targetResumeId;

  return <>
    <MainColumn>
      {title}
      <div className="paid-page">
        <div className="paid-head">
          <strong style={{ fontSize: 16 }}>{amMentor ? `요청자 · ${requester} (@${summary.requester.username})` : `현직자 · ${mentor} (@${summary.mentor.username})`}</strong>
          <Badge tone={status.tone}>{status.label}</Badge>
        </div>
        <p className="small muted">{dateText(summary.createdAt)} 요청 · {summary.credits.toLocaleString('ko-KR')} 크레딧</p>
        <section style={{ display: 'grid', gap: 8 }}>
          <h2>요청 내용</h2>
          <p className="paid-quote">{detail.content || '요청 내용 없이 이력서 전체 피드백을 요청했어요.'}</p>
        </section>
        <section style={{ display: 'grid', gap: 8 }}>
          <h2>첨부 이력서 · {summary.resumeTitle}</h2>
          <ResumePreview type={type} id={resumeId} load={loadResume} />
        </section>
        <section style={{ display: 'grid', gap: 8 }}>
          <h2>현직자 답변</h2>
          {detail.responses.length === 0 ? (
            <p className="small muted">{amMentor ? '아직 답변하지 않았어요.' : '현직자가 답변하면 여기에 보여요.'}</p>
          ) : detail.responses.map((r) => (
            <div key={r.id} className="paid-answer"><div className="xs muted" style={{ marginBottom: 6 }}>{dateText(r.createdAt)}</div>{r.content}</div>
          ))}
        </section>
        {canAnswer && (
          <form className="paid-form" onSubmit={submit}>
            <label htmlFor="paid-answer">{detail.responses.length === 0 ? '피드백 답변' : '답변 더 남기기'}</label>
            <textarea id="paid-answer" rows={6} value={answer} disabled={busy} onChange={(event) => setAnswer(event.target.value)}
              placeholder="좋은 점 하나, 고칠 점 하나, 고친 예시 문장을 함께 적어 주세요." />
            {formError && <p role="alert" className="small paid-error">{formError}</p>}
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button type="submit" disabled={busy}>{busy ? '등록하는 중…' : '답변 등록'}</Button>
            </div>
          </form>
        )}
        {!amMentor && <p className="xs muted">다른 현직자에게도 받아 보고 싶다면 <Link href="/mentors">현직자 피드백</Link>에서 요청할 수 있어요.</p>}
      </div>
    </MainColumn>
    <RightAside label="답변 안내">
      <section className="card" style={{ gap: 6 }}>
        <h2>답변 가이드</h2>
        <p className="text-2">1. 좋은 점 하나, 고칠 점 하나</p>
        <p className="text-2">2. 고친 예시 문장 함께 적기</p>
        <p className="text-2">3. 첫 답변을 등록하면 완료되고 크레딧이 적립돼요</p>
      </section>
    </RightAside>
  </>;
}
