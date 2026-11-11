import ProfilePage from '@/views/ProfilePage.jsx';

export const metadata = {
  title: '프로필',
  robots: { index: false },
};

export default async function Page({ params }) {
  const { username } = await params;
  return <ProfilePage username={username} />;
}
