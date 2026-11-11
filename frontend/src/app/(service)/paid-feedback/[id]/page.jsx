import PaidFeedbackDetailPage from '@/views/PaidFeedbackDetailPage.jsx';

export const metadata = { title: '유료 피드백 요청', robots: { index: false } };

export default async function Page({ params }) {
  const { id } = await params;
  return <PaidFeedbackDetailPage key={id} id={id} />;
}
