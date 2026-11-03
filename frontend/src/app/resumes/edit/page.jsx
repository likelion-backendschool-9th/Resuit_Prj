import ResumeEditPage from '@/views/ResumeEditPage.jsx';

export const metadata = { title: '특화 이력서 작성', robots: { index: false } };

// /resumes/edit?job=공고ID&mode=base|blank
export default async function Page({ searchParams }) {
  const { job, mode } = await searchParams;
  return <ResumeEditPage jobId={job} mode={mode} />;
}
