import { notFound } from 'next/navigation';
import ProfilePage from '@/views/ProfilePage.jsx';

const backend = process.env.RESUIT_BACKEND_ORIGIN || 'http://localhost:8080';

async function loadProfile(username) {
  try {
    const response = await fetch(`${backend}/api/v1/users/${encodeURIComponent(username)}`,
      { cache: 'no-store' });
    if (!response.ok) return null;
    return response.json();
  } catch {
    return null;
  }
}

export async function generateMetadata({ params }) {
  const { username } = await params;
  const profile = await loadProfile(username);
  if (!profile) return { title: '프로필을 찾을 수 없습니다', robots: { index: false } };
  return { title: `${profile.nickname || profile.username}님의 프로필`,
    description: profile.selfIntroduction || `${profile.username}님의 프로필과 활동` };
}

export default async function Page({ params }) {
  const { username } = await params;
  const profile = await loadProfile(username);
  if (!profile) notFound();
  return <ProfilePage username={username} initialProfile={profile} />;
}
