'use client';

import { useState, useTransition } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAppState } from '../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge, { POST_TYPE_BADGE } from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import SearchBox from '../components/common/SearchBox.jsx';
import { PostImage, PostVideo } from '../components/feed/PostEmbeds.jsx';
import PostContent from '../components/feed/PostContent.jsx';
import LikeButton from '../components/feed/LikeButton.jsx';
import CommentsSection from '../components/feed/CommentsSection.jsx';
import PostOwnerActions from '../components/feed/PostOwnerActions.jsx';
import './ThreadPage.css';

export default function ThreadPage({ initialPost, error }) {
  const router = useRouter();
  const { me, loggedIn } = useAppState();
  const [updatedPost, setUpdatedPost] = useState(null);
  const [refreshing, startTransition] = useTransition();
  const post = updatedPost || initialPost;
  const badge = post && POST_TYPE_BADGE[post.type];

  return <>
    <MainColumn>
      <div className="page-head">
        <Link href="/" className="icon-btn icon-btn--plain" aria-label="피드로 돌아가기"><Icon name="back" /></Link>
        <h1>게시글</h1>
      </div>
      {!post ? <div className="empty">
        <p role="alert">{error || '게시글을 찾을 수 없어요.'}</p>
        <Button variant="outline" disabled={refreshing} onClick={() => startTransition(() => router.refresh())}>
          {refreshing ? '불러오는 중…' : '다시 시도'}
        </Button>
      </div> : <>
        <article className="thread-post">
          <header className="thread-post__header">
            <Avatar initial={post.initial} src={post.profileImage} size={48} />
            <div className="thread-post__author">
              <Link href={`/${encodeURIComponent(post.handle)}`}><strong>{post.author}</strong></Link>
              <span className="small muted">@{post.handle}</span>
            </div>
            {badge && <Badge tone={badge.tone}>{badge.label}</Badge>}
          </header>
          <PostContent content={post.body} textClassName="thread-post__text" />
          {post.image && <PostImage src={post.image} alt={post.imageAlt} />}
          {post.video && <PostVideo src={post.video} alt={`${post.author}님의 첨부 영상`} />}
          {post.blogPostUrl && <a className="embed" href={post.blogPostUrl} target="_blank" rel="noopener noreferrer">블로그 원문 보기</a>}
          {post.repoUrl && <a className="embed" href={post.repoUrl} target="_blank" rel="noopener noreferrer">{post.repoFullName}</a>}
          <p className="thread-post__time">{post.time}</p>
          {loggedIn && post.handle === me.handle && <PostOwnerActions post={post} onUpdated={setUpdatedPost}
            onDeleted={() => { router.replace('/'); router.refresh(); }} />}
          <div className="thread-post__actions" aria-label="게시글 반응">
            <a href="#thread-reply" className="icon-btn icon-btn--plain" aria-label="댓글 작성란으로 이동"><Icon name="msg" /></a>
            <button className="icon-btn icon-btn--plain" disabled aria-label="리포스트 준비 중" title="리포스트 준비 중"><Icon name="repeat" /></button>
            <LikeButton post={post} />
          </div>
        </article>
        <CommentsSection key={post.id} post={post} />
      </>}
    </MainColumn>
    <RightAside label="검색 및 추후 활용 영역"><SearchBox /></RightAside>
  </>;
}
