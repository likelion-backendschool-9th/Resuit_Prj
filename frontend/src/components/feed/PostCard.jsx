import Link from 'next/link';
import { useRouter } from 'next/navigation';
import PostOwnerActions from './PostOwnerActions.jsx';
import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import Badge, { POST_TYPE_BADGE } from '../common/Badge.jsx';
import FollowButton from '../common/FollowButton.jsx';
import Icon from '../common/Icon.jsx';
import { TechTags, PostImage, PostVideo, BlogEmbed, RepoEmbed, Evidence } from './PostEmbeds.jsx';
import CodeBlock from './CodeBlock.jsx';
import JobCard from '../jobs/JobCard.jsx';
import './PostCard.css';

const REPLY_LABEL = { question: '답변', repo: '리뷰' };

export default function PostCard({ post, onUpdated, onDeleted }) {
  const router = useRouter();
  const { me, loggedIn, liked, toggleLike } = useAppState();
  const isLiked = !!liked[post.id];
  const typeBadge = POST_TYPE_BADGE[post.type];
  const job = post.type === 'job' ? post.job : null;
  const detailLink = post.source === 'api' ? `/threads/${post.id}` : null;
  const threadLink = post.threadId ? `/threads/${post.threadId}` : null;

  return (
    <article className={`post-card${detailLink ? ' post-card--linked' : ''}`} onClick={(event) => {
      if (!detailLink || event.defaultPrevented || event.target.closest('a, button, input, textarea, select, label, form, video')) return;
      if (window.getSelection()?.toString()) return;
      router.push(detailLink);
    }}>
      <Avatar initial={post.initial} color={post.av} company={post.company} src={post.profileImage} />
      <div className="post-card__body">
        <header className="post-card__head">
          <strong>{post.author}</strong>
          {post.company && <Badge tone="company">기업</Badge>}
          {post.sharerBadge && <Badge tone="sharer">{post.sharerBadge}</Badge>}
          <span className="muted">@{post.handle} · {post.time}</span>
          {typeBadge && <Badge tone={typeBadge.tone}>{typeBadge.label}</Badge>}
          {post.source !== 'api' && post.handle && post.handle !== me.handle && (
            <FollowButton handle={post.handle} className="post-card__follow" />
          )}
        </header>

        {detailLink ? <Link href={detailLink} className="post-card__detail-link" aria-label={`${post.author}님의 게시글 상세 보기`}>
          <p className="post-card__text">{post.body || '게시글 보기'}</p>
        </Link> : post.body && <p className="post-card__text">{post.body}</p>}

        {post.code && <CodeBlock code={post.code} />}
        {post.image && <PostImage src={post.image} alt={post.imageAlt} />}
        {post.video && <PostVideo src={post.video} poster={post.poster} alt={post.imageAlt} duration={post.duration} />}
        {post.type === 'tech' && post.source !== 'api' && <TechTags tags={post.tags} source={post.source} />}
        {post.blog && <BlogEmbed blog={post.blog} />}
        {post.repo && <RepoEmbed repo={post.repo} />}
        {post.blogPostUrl && <a className="embed" href={post.blogPostUrl} target="_blank" rel="noopener noreferrer">블로그 원문 보기</a>}
        {post.repoUrl && <a className="embed" href={post.repoUrl} target="_blank" rel="noopener noreferrer">{post.repoFullName}</a>}
        {post.evidence && <Evidence text={post.evidence} />}
        {job && <JobCard job={job} compact meta={post.jobMeta} sub={post.jobSub} />}
        {post.type === 'question' && threadLink && (
          <Link href={threadLink} className="post-card__question-link">답변 {post.replies || 0}개 보기</Link>
        )}

        {post.source === 'api' && loggedIn && post.handle === me.handle && onUpdated && onDeleted && (
          <PostOwnerActions post={post} onUpdated={onUpdated} onDeleted={onDeleted} />
        )}
        {post.source !== 'api' && <div className="post-card__actions">
          <button type="button" className="post-card__action">
            <Icon name="reply" size={18} />
            <span>{REPLY_LABEL[post.type] || '답글'} {post.replies || 0}</span>
          </button>
          <button type="button" className="post-card__action">
            <Icon name="repost" size={18} />
            <span>{post.reposts || 0}</span>
          </button>
          <button
            type="button"
            className={`post-card__action${isLiked ? ' is-liked' : ''}`}
            aria-pressed={isLiked}
            onClick={() => toggleLike(post.id)}
          >
            <Icon name={isLiked ? 'heartFill' : 'heart'} size={18} />
            <span>{(post.likes || 0) + (isLiked ? 1 : 0)}</span>
          </button>
          <button type="button" className="post-card__action" aria-label="공유">
            <Icon name="share" size={18} />
          </button>
        </div>}
      </div>
    </article>
  );
}
