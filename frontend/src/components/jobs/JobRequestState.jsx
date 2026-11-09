import Button from '@/components/common/Button.jsx';
export default function JobRequestState({ loading, error, retry }) {
  if (loading) return <p className="empty" role="status">공고를 불러오는 중…</p>;
  if (error) return <div className="empty"><p role="alert">{error}</p><Button variant="outline" onClick={retry}>다시 시도</Button></div>;
  return null;
}
