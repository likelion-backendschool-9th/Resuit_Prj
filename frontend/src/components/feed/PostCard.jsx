import Link from 'next/link';
import { useState } from 'react';
import LikeButton from './LikeButton.jsx';
import CommentModal from './CommentModal.jsx';
import { useRouter } from 'next/navigation';
import PostOwnerActions from './PostOwnerActions.jsx';
import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import Badge, { POST_TYPE_BADGE } from '../common/Badge.jsx';
import FollowButton from '../common/FollowButton.jsx';
import Icon from '../common/Icon.jsx';
import { TechTags, PostImage, PostVideo, BlogEmbed, RepoEmbed, Evidence } from './PostEmbeds.jsx';
import PostContent from './PostContent.jsx';
import CodeBlock from './CodeBlock.jsx';
import JobCard from '../jobs/JobCard.jsx';
import './PostCard.css';


export default function PostCard({ post, onUpdated, onDeleted }) {
  const router = useRouter();
  const [replyOpen, setReplyOpen] = useState(false);
  const [replyNotice, setReplyNotice] = useState('');
  const [commentCount, setCommentCount] = useState(post.commentCount ?? 0);
  const { me, loggedIn, liked, toggleLike } = useAppState();
  const isLiked = !!liked[post.id];
  const typeBadge = POST_TYPE_BADGE[post.type];
  const job = post.type === 'job' ? post.job : null;
  const detailLink = post.detailLink || (post.source === 'api' ? `/threads/${post.id}` : null);
  const threadLink = post.threadId ? `/threads/${post.threadId}` : null;
  // 내 글은 세션 이미지를 우선해, 피드 캐시에 옛 URL이 남아 있어도 프로필 변경이 바로 보이게 한다.
  const avatarSrc = (loggedIn && post.handle === me.handle && me.profileImage) || post.profileImage || undefined;

  return (
    <article className={`post-card${detailLink ? ' post-card--linked' : ''}${post.source === 'api' && loggedIn && post.handle === me.handle && onUpdated && onDeleted ? ' post-card--owned' : ''}`} onClick={(event) => {
      if (!detailLink || event.defaultPrevented || event.target.closest('a, button, input, textarea, select, label, form, video')) return;
      if (window.getSelection()?.toString()) return;
      router.push(detailLink);
    }}>
      <Avatar initial={post.initial} color={post.av} company={post.company} src={avatarSrc} />
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

        <PostContent content={post.body} detailLink={detailLink} />
        {detailLink && <Link href={detailLink} className="sr-only">{post.author}님의 게시글 상세 보기</Link>}

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
        {post.source === 'api' && <>
          <div className="post-card__actions">
            <button type="button" className="post-card__action" aria-label="댓글 작성" onClick={() => { setReplyNotice(''); setReplyOpen(true); }}>
              <Icon name="msg" size={20} /><span>{commentCount}</span>
            </button>
            <button type="button" className="post-card__action" aria-label="리트윗 준비 중" title="리트윗 준비 중" disabled>
              <Icon name="repeat" size={20} />
            </button>
            <LikeButton post={post} />
          </div>
          {replyNotice && <span className="small muted" role="status">{replyNotice}</span>}
          {replyOpen && <CommentModal post={post} onClose={(saved) => {
            setReplyOpen(false);
            if (saved === true) {
              setCommentCount((count) => count + 1);
              setReplyNotice('댓글을 등록했어요. 상세 게시글에서 확인할 수 있어요.');
            }
          }} />}
        </>}
        {post.source !== 'api' && <div className="post-card__actions">
          <button type="button" className="post-card__action" aria-label="댓글 보기" disabled={!threadLink} onClick={() => router.push(threadLink)}>
            <Icon name="msg" size={20} />
          </button>
          <button type="button" className="post-card__action" aria-label="리트윗 준비 중" disabled><Icon name="repeat" size={20} /></button>
          <button type="button" className={`post-card__action${isLiked ? ' is-liked' : ''}`} aria-label="좋아요" aria-pressed={isLiked} onClick={() => toggleLike(post.id)}>
            <Icon name="heart" filled={isLiked} size={20} />
          </button>
        </div>}
      </div>
    </article>
  );
}
