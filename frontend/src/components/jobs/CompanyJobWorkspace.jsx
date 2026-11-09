import { useCallback, useState } from 'react';
import Link from 'next/link';
import Button from '@/components/common/Button.jsx';
import JobEditor from './JobEditor.jsx';
import JobOwnerActions from './JobOwnerActions.jsx';
import JobRequestState from './JobRequestState.jsx';
import useJobResource from './useJobResource.js';
import { getJob, getJobDraft, getJobDrafts } from '@/services/jobs.js';

function SelectedEditor({ id, draft, owner, onSaved }) {
  const load = useCallback((signal) => id ? (draft ? getJobDraft(id, { signal }) : getJob(id, { signal })) : Promise.resolve({ fresh: true }), [id, draft]);
  const state = useJobResource(load);
  if (state.loading || state.error) return <JobRequestState {...state} />;
  if (id && state.data?.company.username !== owner) return <p className="empty" role="alert">본인이 작성한 공고만 수정할 수 있어요.</p>;
  return <><JobEditor key={id || 'new'} job={id ? state.data : null} onSaved={onSaved} />{id && draft && <JobOwnerActions job={state.data} onDeleted={onSaved} />}</>;
}

export default function CompanyJobWorkspace({ id, draft, owner }) {
  const [page, setPage] = useState(0);
  const load = useCallback((signal) => getJobDrafts(page, { signal }), [page]);
  const state = useJobResource(load);
  return <>
    <div className="job-drafts">
      <details open={Boolean(id && draft)}><summary>내 임시저장 공고{state.data ? ` (${state.data.totalElements})` : ''}</summary>
        <JobRequestState {...state} />
        {state.data && <><ul className="job-drafts__list">{state.data.content.map((job) => <li key={job.id}><Link className="text-link" href={`/company/post?draft=${job.id}`}>{job.title}</Link></li>)}</ul>
          {!state.data.content.length && <p className="small muted">임시저장한 공고가 없어요.</p>}
          <div className="jobs-pagination"><Button variant="outline" disabled={page === 0 || state.loading} onClick={() => setPage(page - 1)}>이전</Button><span>{page + 1} / {Math.max(1, state.data.totalPages)}</span><Button variant="outline" disabled={state.data.last || state.loading} onClick={() => setPage(page + 1)}>다음</Button></div>
        </>}
      </details>
    </div>
    {state.loading ? <p className="empty" role="status">기업 계정 권한을 확인하고 있어요.</p> : state.error ? <div className="section"><p role="alert">{state.error}</p><Button variant="outline" onClick={state.retry}>다시 확인</Button><Button to="/profile/verification" variant="outline">기업 인증</Button></div>
      : <SelectedEditor key={`${draft ? 'draft' : 'job'}-${id || 'new'}`} id={id} draft={draft} owner={owner} onSaved={state.retry} />}
  </>;
}
