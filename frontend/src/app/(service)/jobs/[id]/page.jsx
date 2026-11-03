import { JOBS } from '@/data/mock.js';
import JobDetailPage from '@/views/JobDetailPage.jsx';

// 공고마다 검색 결과와 링크 미리보기에 나올 제목 · 설명을 만든다.
export async function generateMetadata({ params }) {
  const { id } = await params;
  const job = JOBS.find((j) => String(j.id) === id) || JOBS[0];
  const title = `${job.title} · ${job.company}`;
  return { title, description: job.intro, openGraph: { title, description: job.intro } };
}

export default async function Page({ params }) {
  const { id } = await params;
  return <JobDetailPage id={id} />;
}
