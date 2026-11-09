import { Suspense } from 'react';
import CreditPaymentFailPage from '@/views/CreditPaymentFailPage.jsx';

export const metadata = { title: '크레딧 충전 실패', robots: { index: false } };

// 화면이 주소의 쿼리(message 등)를 읽으므로 Suspense로 감싼다(Next.js useSearchParams 규칙).
export default function Page() { return <Suspense><CreditPaymentFailPage /></Suspense>; }
