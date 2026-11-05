import JobDetailPage from '@/views/JobDetailPage.jsx';

export async function generateMetadata({ params }) {
  const { id } = await params;
  const title = '채용 공고';
  const description = '공고 상세';
  return {
    title,
    description,
    openGraph: { title, description },
  };
}

export default async function Page({ params }) {
  const { id } = await params;
  return <JobDetailPage id={id} />;
}
