'use client';

import { useCallback, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '@/context/AppStateContext.jsx';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import useJobResource from '@/components/jobs/useJobResource.js';
import { PostedJobs, Applicants, ApplicationDetail, ApplicationJobAside, ApplicationsPagination } from '@/components/jobs/CompanyApplications.jsx';
import { getPostedJobs, getCompanyApplicants, getCompanyApplication } from '@/services/jobs.js';
import '@/components/jobs/Jobs.css';

function ApplicationsWorkspace({ jobId, applicationId }) {
  const [page, setPage] = useState(0);
  const load = useCallback((signal) => applicationId ? getCompanyApplication(jobId, applicationId, { signal })
    : jobId ? getCompanyApplicants(jobId, page, { signal }) : getPostedJobs(page, { signal }), [jobId, applicationId, page]);
  const { data, loading, error, retry } = useJobResource(load);
  const job = data?.job;
  const title = applicationId ? '제출 이력서' : jobId ? '공고 지원자' : '지원 현황';
  const backTo = applicationId ? `/company/applications/${jobId}` : '/company/applications';
  return <>
    <MainColumn>
      {jobId && <div className="company-applications__breadcrumb"><Link href={backTo} className="text-link">← {applicationId ? '지원자 목록' : '게시 공고 목록'}</Link></div>}
      <div className="page-title"><h1>{title}</h1></div>
      <p className="page-desc">{job?.title || '게시한 공고의 지원자와 제출 당시 이력서를 확인하세요.'}</p>
      {loading ? <p className="empty" role="status">지원 현황을 불러오는 중이에요.</p>
        : error ? <div className="empty"><p role="alert">{error}</p><Button variant="outline" onClick={retry}>다시 시도</Button></div>
          : data && <div className="jobs-content">
            {applicationId ? <ApplicationDetail {...data} />
              : jobId ? <Applicants {...data} /> : <PostedJobs data={data} />}
            {!applicationId && <ApplicationsPagination data={jobId ? data.applications : data} page={page} onPageChange={setPage} />}
          </div>}
    </MainColumn>
    <RightAside label="지원 현황 안내"><ApplicationJobAside job={job} /></RightAside>
  </>;
}

export default function CompanyApplicationsPage({ jobId, applicationId }) {
  const { loggedIn, authLoading, me } = useAppState();
  const valid = (!jobId || /^[1-9]\d*$/.test(jobId)) && (!applicationId || /^[1-9]\d*$/.test(applicationId));
  if (!authLoading && loggedIn && me.role === 'COMPANY' && valid)
    return <ApplicationsWorkspace key={`${me.id}:${jobId || ''}:${applicationId || ''}`} jobId={jobId} applicationId={applicationId} />;
  return <>
    <MainColumn>
      <div className="page-title"><h1>지원 현황</h1></div>
      <div className="empty" role="status">
        {authLoading ? '로그인 상태를 확인하고 있어요.'
          : !loggedIn ? <><p>기업 계정으로 로그인하면 지원 현황을 확인할 수 있어요.</p><Button to="/login">로그인</Button></>
            : !valid ? <><p>올바르지 않은 지원 현황 주소입니다.</p><Button to="/company/applications">게시 공고 목록</Button></>
              : <p>기업 계정만 지원 현황을 조회할 수 있어요.</p>}
      </div>
    </MainColumn>
    <RightAside label="지원 현황 안내"><ApplicationJobAside /></RightAside>
  </>;
}
