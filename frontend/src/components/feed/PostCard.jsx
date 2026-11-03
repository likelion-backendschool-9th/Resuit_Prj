import { Link } from 'react-router-dom';
import { useAppState } from '../../context/AppStateContext.jsx';
import { SHARER_BADGES, JOBS } from '../../data/mock.js';
import Avatar from '../common/Avatar.jsx';
import Badge, { POST_TYPE_BADGE } from '../common/Badge.jsx';
import FollowButton from '../common/FollowButton.jsx';
import Icon from '../common/Icon.jsx';
import { TechTags, PostImage, PostVideo, BlogEmbed, RepoEmbed, Evidence } from './PostEmbeds.jsx';
import CodeBlock from './CodeBlock.jsx';
import JobCard from '../jobs/JobCard.jsx';
import './PostCard.css';

const REPLY_LABEL = { question: '답변', repo: '리뷰' };

export default function PostCard({ post }) {
  const { me, liked, toggleLike } = useAppState();
  const isLiked = !!liked[post.id];
  const typeBadge = POST_TYPE_BADGE[post.type];
  const sharer = SHARER_BADGES[post.handle];
  const job = post.type === 'job' ? JOBS.find((j) => j.id === post.jobId) : null;
  const threadLink = post.threadId ? `/threads/${post.threadId}` : '/threads/1';

  return (
    <article className="post-card">
      <Avatar initial={post.initial} color={post.av} company={post.company} />
      <div className="post-card__body">
        <header className="post-card__head">
          <strong>{post.author}</strong>
          {post.company && <Badge tone="company">기업</Badge>}
          {sharer && <Badge tone="sharer">{sharer}</Badge>}
          <span className="muted">@{post.handle} · {post.time}</span>
          {typeBadge && <Badge tone={typeBadge.tone}>{typeBadge.label}</Badge>}
          {post.handle !== me.handle && <FollowButton handle={post.handle} className="post-card__follow" />}
        </header>

        {post.body && <p className="post-card__text">{post.body}</p>}

        {post.code && <CodeBlock code={post.code} />}
        {post.image && <PostImage src={post.image} alt={post.imageAlt} />}
        {post.video && <PostVideo src={post.video} poster={post.poster} alt={post.imageAlt} duration={post.duration} />}
        {post.type === 'tech' && <TechTags tags={post.tags} source={post.source} />}
        {post.blog && <BlogEmbed blog={post.blog} />}
        {post.repo && <RepoEmbed repo={post.repo} />}
        {post.evidence && <Evidence text={post.evidence} />}
        {job && <JobCard job={job} compact meta={post.jobMeta} sub={post.jobSub} />}
        {post.type === 'question' && (
          <Link to={threadLink} className="post-card__question-link">답변 {post.replies}개 보기</Link>
        )}

        <div className="post-card__actions">
          <Link to={threadLink} className="post-card__action" aria-label="답글">
            <Icon name="msg" size={18} />{REPLY_LABEL[post.type] || '댓글'} {post.replies}
          </Link>
          <button type="button" className="post-card__action" aria-label="리포스트">
            <Icon name="repeat" size={18} />{post.reposts}
          </button>
          <button
            type="button"
            className={`post-card__action${isLiked ? ' is-liked' : ''}`}
            aria-label="좋아요"
            aria-pressed={isLiked}
            onClick={() => toggleLike(post.id)}
          >
            <Icon name="heart" size={18} filled={isLiked} />{post.likes + (isLiked ? 1 : 0)}
          </button>
          <button type="button" className="post-card__action" aria-label="북마크">
            <Icon name="bookmark" size={18} />
          </button>
        </div>
      </div>
    </article>
  );
}
