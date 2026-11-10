import AppLayout from '@/components/layout/AppLayout.jsx';
import PageAccessGate from '@/components/auth/PageAccessGate.jsx';

// 일반 서비스 페이지의 공통 3단 레이아웃. (service) 는 주소에 나타나지 않는다.
export default function ServiceLayout({ children }) {
  return <AppLayout><PageAccessGate>{children}</PageAccessGate></AppLayout>;
}
