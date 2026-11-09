import CompanyPostPage from '@/views/CompanyPostPage.jsx';

export const metadata = { title: '채용 공고 작성·관리', robots: { index: false } };

export default async function Page({ searchParams }) {
  const query = await searchParams;
  const id = query.draft || query.job;
  const jobId = typeof id === 'string' && /^[1-9]\d*$/.test(id) ? id : null;
  return <CompanyPostPage jobId={jobId} draft={Boolean(query.draft && jobId)} />;
}
