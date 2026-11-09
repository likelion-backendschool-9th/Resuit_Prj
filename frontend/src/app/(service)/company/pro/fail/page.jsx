import { Suspense } from 'react';
import ProPaymentFailPage from '@/views/ProPaymentFailPage.jsx';

export const metadata = { title: '기업 Pro 결제 실패', robots: { index: false } };

export default function Page() { return <Suspense><ProPaymentFailPage /></Suspense>; }
