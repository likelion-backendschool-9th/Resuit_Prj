import { StarIcon } from './Icon.jsx';
import './Badge.css';

// tone: company | question | tech | blog | repo | special | general | accent | sharer | accepted
export default function Badge({ tone = 'general', size = 'sm', children }) {
  return (
    <span className={`badge badge--${tone} badge--${size}`}>
      {tone === 'sharer' && <StarIcon />}
      {children}
    </span>
  );
}

// 게시글 type → 배지
export const POST_TYPE_BADGE = {
  tech: { tone: 'tech', label: '기술 정보' },
  question: { tone: 'question', label: '질문' },
  blog: { tone: 'blog', label: '블로그 글' },
  repo: { tone: 'repo', label: '레포 공유' }
};
