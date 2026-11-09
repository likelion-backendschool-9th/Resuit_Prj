import { Suspense } from 'react';
import ProPaymentSuccessPage from '@/views/ProPaymentSuccessPage.jsx';

export const metadata = { title: '기업 Pro 결제 완료', robots: { index: false } };

// 화면이 주소의 쿼리(paymentKey 등)를 읽으므로 Suspense로 감싼다(Next.js useSearchParams 규칙).
export default function Page() { return <Suspense><ProPaymentSuccessPage /></Suspense>; }
