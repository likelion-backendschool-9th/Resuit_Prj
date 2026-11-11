import ReviewHighlights from '../reviews/ReviewHighlights.jsx';
import LeftNav from './LeftNav.jsx';
import './AppLayout.css';

// 3단 레이아웃: [왼쪽 메뉴] [가운데 본문] [오른쪽 사이드바]
// 가운데와 오른쪽은 각 페이지가 <MainColumn>, <RightAside> 로 채운다.
export default function AppLayout({ children }) {
  return (
    <div className="app-layout">
      <LeftNav />
      {children}
    </div>
  );
}

export function MainColumn({ children }) {
  return <main className="main-column">{children}</main>;
}

// label 만 주고 children 을 비워 두면 "추후 활용 영역"으로 폭만 차지한다.
export function RightAside({ children, label, showReviews = false }) {
  return (
    <aside className="right-aside" aria-label={label}>
      {children}
      {showReviews && <ReviewHighlights />}
    </aside>
  );
}
