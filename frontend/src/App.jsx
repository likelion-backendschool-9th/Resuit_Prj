import { Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/layout/AppLayout.jsx';
import OnboardingPage from './pages/OnboardingPage.jsx';
import FeedPage from './pages/FeedPage.jsx';
import JobsPage from './pages/JobsPage.jsx';
import JobDetailPage from './pages/JobDetailPage.jsx';
import ResumeEditPage from './pages/ResumeEditPage.jsx';
import ResumesPage from './pages/ResumesPage.jsx';
import FeedbackPage from './pages/FeedbackPage.jsx';
import ThreadPage from './pages/ThreadPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import CompanyPostPage from './pages/CompanyPostPage.jsx';
import TalentSearchPage from './pages/TalentSearchPage.jsx';

// 라우트 한눈에 보기
// /onboarding          시작하기 (GitHub 로그인 · 블로그 RSS)
// /                    홈 피드
// /jobs                공고 목록
// /jobs/:id            공고 상세 (특화: 특화 이력서 작성 / 일반: 공통 이력서 선택 후 지원)
// /resumes             이력서 › 내 이력서
// /resumes/edit        특화 이력서 작성 (?mode=base|blank)
// /feedback            이력서 › 피드백 게시판
// /threads/:id         질문 스레드
// /profile             프로필
// /company/post        회사 공고 작성
// /talent              인재 찾기 (기업 회원 전용)
export default function App() {
  return (
    <Routes>
      <Route path="/onboarding" element={<OnboardingPage />} />
      <Route path="/resumes/edit" element={<ResumeEditPage />} />
      <Route element={<AppLayout />}>
        <Route path="/" element={<FeedPage />} />
        <Route path="/jobs" element={<JobsPage />} />
        <Route path="/jobs/:id" element={<JobDetailPage />} />
        <Route path="/resumes" element={<ResumesPage />} />
        <Route path="/feedback" element={<FeedbackPage />} />
        <Route path="/threads/:id" element={<ThreadPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/company/post" element={<CompanyPostPage />} />
        <Route path="/talent" element={<TalentSearchPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
