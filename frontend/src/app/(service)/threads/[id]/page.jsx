import { THREADS } from '@/data/mock.js';
import ThreadPage from '@/views/ThreadPage.jsx';

export async function generateMetadata({ params }) {
  const { id } = await params;
  const thread = THREADS[id] || THREADS[1];
  return { title: thread.body, description: thread.body, openGraph: { title: thread.body, description: thread.body } };
}

export default async function Page({ params }) {
  const { id } = await params;
  return <ThreadPage id={id} />;
}
