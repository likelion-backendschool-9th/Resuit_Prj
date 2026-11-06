import Link from 'next/link';
import { parsePostContent } from '../../utils/postContent.js';
import CodeBlock from './CodeBlock.jsx';

export default function PostContent({ content, textClassName = 'post-card__text', detailLink }) {
  return parsePostContent(content).map((block, index) => {
    if (block.type === 'code') return <CodeBlock key={index} code={block.code} />;
    if (!block.text.trim()) return null;
    const text = <p className={textClassName}>{block.text}</p>;
    return detailLink ? <Link key={index} href={detailLink} className="post-card__detail-link">{text}</Link> : <div key={index}>{text}</div>;
  });
}
