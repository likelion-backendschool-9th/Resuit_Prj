import FeedbackDetailPage from '@/views/FeedbackDetailPage.jsx';

export const metadata = { title: '피드백 요청', robots: { index: false } };

export default async function Page({ params }) {
  const { id } = await params;
  return <FeedbackDetailPage key={id} id={id} />;
}
