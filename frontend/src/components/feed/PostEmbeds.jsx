import Link from 'next/link';
import Icon from '../common/Icon.jsx';
import './PostEmbeds.css';

// 기술 정보 글: 태그 + 출처
export function TechTags({ tags = [], source }) {
  return (
    <div className="tech-tags">
      {tags.map((t) => <span key={t} className="hashtag">#{t}</span>)}
      {source && <span className="tech-tags__source"><Icon name="link" size={13} />출처 · {source}</span>}
    </div>
  );
}

// 글에 첨부한 사진
export function PostImage({ src, alt }) {
  return <img className="post-media" src={src} alt={alt} loading="lazy" />;
}

// 글에 첨부한 영상 (소리 없이 자동 재생)
export function PostVideo({ src, poster, alt, duration }) {
  return (
    <div className="post-video">
      <video className="post-media post-media--video" src={src} poster={poster} aria-label={alt} controls muted loop playsInline autoPlay preload="metadata" />
      <span className="post-video__label">영상 · {duration}</span>
    </div>
  );
}

// RSS로 자동 공유된 블로그 글
export function BlogEmbed({ blog }) {
  return (
    <Link href="/profile" className="embed">
      <span className="embed__kicker"><Icon name="rss" size={14} />{blog.source} · RSS로 자동 공유</span>
      <span className="embed__title">{blog.title}</span>
      <span className="small muted">{blog.desc}</span>
    </Link>
  );
}

// GitHub 레포 공유
export function RepoEmbed({ repo }) {
  return (
    <Link href="/profile" className="embed">
      <span className="row"><Icon name="repo" size={18} /><strong>{repo.name}</strong><span className="embed__pill">Public</span></span>
      <span className="text-2" style={{ fontSize: 14 }}>{repo.desc}</span>
      <span className="embed__meta">
        <span className="row" style={{ gap: 6 }}><span className="lang-dot" style={{ background: repo.langColor }} />{repo.lang}</span>
        <span>Star {repo.stars}</span>
        <span>{repo.updated}</span>
      </span>
    </Link>
  );
}

// "이력서 프로젝트의 근거로 연결됨" 표시
export function Evidence({ text }) {
  return <span className="evidence"><Icon name="link" size={14} />{text}</span>;
}
