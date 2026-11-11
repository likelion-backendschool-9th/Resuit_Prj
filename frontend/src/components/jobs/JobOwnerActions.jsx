import { useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import Button from '@/components/common/Button.jsx';
import { deleteJob } from '@/services/jobs.js';

export default function JobOwnerActions({ job, onDeleted }) {
  const router = useRouter();
  const pending = useRef(false);
  const [confirm, setConfirm] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function remove() {
    if (pending.current) return;
    pending.current = true; setBusy(true); setError('');
    try { await deleteJob(job.id); onDeleted?.(); router.replace(job.status === 'DRAFT' ? '/company/post' : '/jobs'); router.refresh(); }
    catch (e) { setError(e.message); }
    finally { pending.current = false; setBusy(false); }
  }
  return <section className="section"><h3>공고 관리</h3>{error && <p role="alert">{error}</p>}
    {confirm ? <><p>이 공고를 삭제할까요? 삭제하면 목록에서 더 이상 표시되지 않습니다.</p><div className="row"><Button disabled={busy} onClick={remove}>{busy ? '삭제 중…' : '삭제 확인'}</Button><Button disabled={busy} variant="outline" onClick={() => setConfirm(false)}>돌아가기</Button></div></>
      : <div className="row">{job.status !== 'DRAFT' && <Button to={`/company/post?job=${job.id}`} variant="outline">수정</Button>}<Button variant="outline" onClick={() => setConfirm(true)}>삭제</Button></div>}
  </section>;
}
