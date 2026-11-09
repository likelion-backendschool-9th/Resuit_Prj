import JobsPage from '@/views/JobsPage.jsx';
import { getJobs } from '@/services/jobs.js';

export const metadata = { title: '채용 공고', description: '일반·특화 채용 공고를 살펴보고 나의 경험과 맞는 다음 기회를 찾아보세요.' };

export default async function Page() {
  let initialJobs = null;
  try { initialJobs = await getJobs({ baseUrl: process.env.BACKEND_URL || 'http://localhost:8080' }); } catch { /* Client retry remains available. */ }
  return <JobsPage initialJobs={initialJobs} />;
}
