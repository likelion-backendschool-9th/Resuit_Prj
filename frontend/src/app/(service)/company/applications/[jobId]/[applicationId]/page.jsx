import CompanyApplicationsPage from '@/views/CompanyApplicationsPage.jsx';

export const metadata = { title: '지원 현황', description: '내 공고의 지원자와 제출 이력서를 확인하세요.', robots: { index: false } };

export default async function Page({ params }) {
  const { jobId, applicationId } = await params;
  return <CompanyApplicationsPage jobId={jobId} applicationId={applicationId} />;
}
