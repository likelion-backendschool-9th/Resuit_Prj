import PageAccessGate from '@/components/auth/PageAccessGate.jsx';

// 서비스 라우트 그룹 밖에 있는 공통·특화 이력서 편집도 같은 접근 규칙을 적용한다.
export default function ResumeEditorLayout({ children }) {
  return <PageAccessGate standalone>{children}</PageAccessGate>;
}
