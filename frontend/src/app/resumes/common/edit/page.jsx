import CommonResumeEditPage from '@/views/CommonResumeEditPage.jsx';

export const metadata = { title: '공통 이력서 편집', robots: { index: false } };

// /resumes/common/edit?id=공통이력서ID
export default async function Page({ searchParams }) {
  const { id } = await searchParams;
  return <CommonResumeEditPage key={id} id={id} />;
}
