import { cookies } from 'next/headers';
import ThreadPage from '@/views/ThreadPage.jsx';
import { getPostDetail } from '@/services/posts.js';

export const metadata = {
  title: '게시글 | Resuit',
  description: '개발자의 게시글과 이야기를 자세히 확인하세요.',
};

export default async function Page({ params }) {
  const { id } = await params;
  let post = null;
  let error = '';
  try {
    post = await getPostDetail(id, { baseUrl: process.env.BACKEND_URL || 'http://localhost:8080',
      headers: { Cookie: (await cookies()).toString() } });
  } catch (err) {
    error = err.message;
  }
  return <ThreadPage key={id} initialPost={post} error={error} />;
}
