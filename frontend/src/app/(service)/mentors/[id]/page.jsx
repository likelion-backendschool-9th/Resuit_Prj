import MentorDetailPage from '@/views/MentorDetailPage.jsx';

export const metadata = { title: '현직자 상세', robots: { index: false } };

export default async function Page({ params }) {
  const { id } = await params;
  return <MentorDetailPage key={id} id={id} />;
}
