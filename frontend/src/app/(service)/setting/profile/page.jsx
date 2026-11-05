import ProfilePage from '@/views/ProfilePage.jsx';

export const metadata = { title: '프로필 설정', robots: { index: false } };

export default function Page() {
  return <ProfilePage startEditing />;
}
