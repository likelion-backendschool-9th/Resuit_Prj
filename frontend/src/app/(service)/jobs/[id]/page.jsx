import { cache } from 'react';
import JobDetailPage from '@/views/JobDetailPage.jsx';
import { getJob } from '@/services/jobs.js';

const load = cache(async (id) => {
  try { return await getJob(id, { baseUrl: process.env.BACKEND_URL || 'http://localhost:8080' }); }
  catch { return null; }
});

export async function generateMetadata({ params }) {
  const { id } = await params;
  const job = await load(id);
  const title = job?.title || '채용 공고';
  const description = job?.description?.slice(0, 160) || '채용 공고의 상세 조건과 요구 문항을 확인하세요.';
  return { title, description, openGraph: { title, description } };
}

export default async function Page({ params }) {
  const { id } = await params;
  return <JobDetailPage key={id} id={id} initialJob={await load(id)} />;
}
