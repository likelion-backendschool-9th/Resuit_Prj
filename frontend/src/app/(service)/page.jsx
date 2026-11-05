import FeedPage from '@/views/FeedPage.jsx';
import { getPosts } from '@/services/posts.js';

export const metadata = {
  title: '피드 | Resuit',
  description: '개발자들의 게시글과 기술 이야기, 질문을 만나보세요.',
};

export default async function Page() {
  let initialTimeline = null;
  try {
    initialTimeline = await getPosts({ baseUrl: process.env.BACKEND_URL || 'http://localhost:8080' });
  } catch {
    // 백엔드에 연결할 수 없어도 공통 레이아웃과 재시도 UI를 표시한다.
  }
  return <FeedPage initialTimeline={initialTimeline} />;
}
