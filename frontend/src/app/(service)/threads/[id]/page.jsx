import ThreadPage from '@/views/ThreadPage.jsx';

export async function generateMetadata({ params }) {
  const { id } = await params;
  return {
    title: '게시글',
    description: '질문과 답변',
    openGraph: { title: '게시글', description: '질문과 답변' },
    robots: { index: false },
  };
}

export default async function Page({ params }) {
  const { id } = await params;
  return <ThreadPage id={id} />;
}
