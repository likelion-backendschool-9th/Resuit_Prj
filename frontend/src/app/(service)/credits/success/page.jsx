import { Suspense } from 'react';
import CreditPaymentSuccessPage from '@/views/CreditPaymentSuccessPage.jsx';

export const metadata = { title: '크레딧 충전 완료', robots: { index: false } };

// 화면이 주소의 쿼리(paymentKey 등)를 읽으므로 Suspense로 감싼다(Next.js useSearchParams 규칙).
export default function Page() { return <Suspense><CreditPaymentSuccessPage /></Suspense>; }
