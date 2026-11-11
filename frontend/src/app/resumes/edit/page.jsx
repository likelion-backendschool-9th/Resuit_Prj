import ResumeEditPage from '@/views/ResumeEditPage.jsx';

export const metadata = { title: '특화 이력서 작성', robots: { index: false } };

// /resumes/edit?id=특화이력서ID (이어서 쓰기)
// /resumes/edit?job=공고ID&mode=base|blank (공고에서 새로 시작)
export default async function Page({ searchParams }) {
  const { id, job, mode } = await searchParams;
  return <ResumeEditPage key={id || `${job}-${mode}`} id={id} jobId={job} mode={mode} />;
}
