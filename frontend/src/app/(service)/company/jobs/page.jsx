import MyJobsPage from '@/views/MyJobsPage.jsx';

export const metadata = {
  title: '내가 작성한 공고',
  description: '내가 게시하거나 임시저장한 채용 공고를 관리하세요.',
  robots: { index: false },
};

export default function Page() {
  return <MyJobsPage />;
}
