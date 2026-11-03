import { redirect } from 'next/navigation';

// 없는 주소는 홈으로 보낸다.
export default function NotFound() {
  redirect('/');
}
