import Link from 'next/link';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import JobCard from './JobCard.jsx';
import ResumeDocument from '../resume/ResumeDocument.jsx';
import './CompanyApplications.css';

export const applicationDate = (value) => value ? value.replace('T', ' ').slice(0, 16) : '정보 없음';
export const applicationJobStatus = (job) => job.status === 'CLOSED' ? '마감' : '게시 중';

export function ApplicationsPagination({ data, page, onPageChange }) {
  if (data.totalPages < 2) return null;
  return <nav className="jobs-pagination" aria-label="지원 현황 페이지">
    <Button variant="outline" disabled={page === 0} onClick={() => onPageChange(page - 1)}>이전</Button>
    <span>{page + 1} / {data.totalPages}</span>
    <Button variant="outline" disabled={data.last} onClick={() => onPageChange(page + 1)}>다음</Button>
  </nav>;
}

export function PostedJobs({ data }) {
  if (!data.content.length) return <div className="empty"><p>아직 게시한 공고가 없어요.</p><Button to="/company/post">공고 작성</Button></div>;
  return <>
    <p className="small muted">게시 공고 {data.totalElements}개 · 최신 등록순</p>
    {data.content.map((job) => <JobCard key={job.id} compact job={{ ...job, type: job.type.toLowerCase() }}
      meta={`${applicationJobStatus(job)} · ${job.deadline ? `${job.deadline} 마감` : '마감일 미정'}`}
      sub={`지원자 ${job.applicationCount}명`}
      actionTo={`/company/applications/${job.id}`} actionLabel={`지원 현황 보기 · ${job.applicationCount}명`} />)}
  </>;
}

export function ApplicantIdentity({ applicant }) {
  const name = applicant.nickname || applicant.username;
  return <div className="company-applications__person">
    <Avatar initial={name.slice(0, 1)} src={applicant.profileImageUrl} />
    <div><strong>{name}</strong><div className="small muted">@{applicant.username}</div></div>
  </div>;
}

export function Applicants({ job, applications }) {
  if (!applications.content.length) return <div className="empty">아직 지원자가 없어요.</div>;
  return <>
    <p className="small muted">지원자 {applications.totalElements}명 · 최신 지원순</p>
    {applications.content.map((item) => <article key={item.applicationId} className="card company-applications__applicant">
      <ApplicantIdentity applicant={item.applicant} />
      <div className="company-applications__resume">
        <Badge tone={item.resumeType === 'TARGET' ? 'special' : 'general'}>{item.resumeType === 'TARGET' ? '특화' : '공통'} 이력서</Badge>
        <strong>{item.resumeTitle || '제출 이력서'}</strong>
        <p className="small muted">지원 {applicationDate(item.appliedAt)}</p>
        {item.snapshotAvailable
          ? <p className="small muted">열람 기한 {applicationDate(item.snapshotExpiresAt)}</p>
          : <p className="small muted">제출 자료의 보존 기간이 만료되었거나 보존된 자료가 없습니다.</p>}
      </div>
      {item.snapshotAvailable
        ? <Button variant="outline" to={`/company/applications/${job.id}/${item.applicationId}`}>제출 이력서 보기</Button>
        : <Button variant="outline" disabled>열람 불가</Button>}
    </article>)}
  </>;
}

export function ApplicationDetail({ application, resume }) {
  return <>
    <section className="card company-applications__detail-header">
      <ApplicantIdentity applicant={application.applicant} />
      <h2>{resume.submission.title}</h2>
      <div className="row-wrap">
        <Badge tone={application.resumeType === 'TARGET' ? 'special' : 'general'}>{application.resumeType === 'TARGET' ? '특화' : '공통'} 이력서</Badge>
        <span className="small muted">지원 {applicationDate(application.appliedAt)}</span>
      </div>
      <p className="small muted">지원 당시 제출한 내용입니다. 이후 원본 이력서의 수정은 반영되지 않습니다.</p>
      <p className="small muted">열람 기한 {applicationDate(application.snapshotExpiresAt)}</p>
    </section>
    <ResumeDocument resume={resume} type={application.resumeType} />
  </>;
}

export function ApplicationJobAside({ job }) {
  return <section className="card company-applications__aside">
    <h2>{job ? '공고 요약' : '지원 현황 안내'}</h2>
    {job && <>
      <strong>{job.title}</strong>
      <Badge tone={job.type === 'SPECIAL' ? 'special' : 'general'}>{job.type === 'SPECIAL' ? '특화' : '일반'} 공고</Badge>
      <p className="small muted">{applicationJobStatus(job)} · {job.deadline ? `${job.deadline} 마감` : '마감일 미정'}</p>
      <p>지원자 <strong>{job.applicationCount}명</strong></p>
      <Link href={`/jobs/${job.id}`} className="text-link">공고 상세 보기</Link>
    </>}
    <p className="small muted">게시한 공고의 지원자만 표시합니다. 취소된 지원은 목록과 인원수에서 제외됩니다.</p>
    <p className="small muted">제출 이력서와 사진은 지원일로부터 3개월 동안 열람할 수 있습니다. 보존 기간이 지나도 지원 기록은 남습니다.</p>
  </section>;
}
