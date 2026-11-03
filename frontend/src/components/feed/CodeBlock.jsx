import { useEffect, useState } from 'react';
import Icon from '../common/Icon.jsx';
import './CodeBlock.css';

const PREVIEW_LINES = 6;
const COPIED_MS = 1500;

const LANGS = { java: 'Java', js: 'JavaScript', jsx: 'JavaScript', ts: 'TypeScript', tsx: 'TypeScript', py: 'Python', sql: 'SQL', kt: 'Kotlin', css: 'CSS', html: 'HTML' };

// 파일 확장자로 언어 이름을 정한다. (글쓰기에서 코드를 붙일 때 사용)
export function langFromFile(file) {
  return LANGS[(file.split('.').pop() || '').toLowerCase()] || 'Text';
}

// GitHub처럼 간단한 문법 색 (주석 · 문자열 · 어노테이션 · 키워드 · 타입)
const TOKEN = /(\/\/.*$)|("(?:[^"\\]|\\.)*")|(@\w+)|\b(public|private|protected|final|class|interface|return|new|if|else|for|while|throw|throws|try|catch|import|package|static|void|int|long|boolean|const|let|var|function|async|await|export|default|from|select|where|limit)\b|\b([A-Z][A-Za-z0-9_]*)\b/g;

function highlight(line) {
  const out = [];
  let last = 0;
  for (const m of line.matchAll(TOKEN)) {
    if (m.index > last) out.push({ text: line.slice(last, m.index) });
    const kind = m[1] ? 'comment' : m[2] ? 'string' : m[3] ? 'annotation' : m[4] ? 'keyword' : 'type';
    out.push({ text: m[0], kind });
    last = m.index + m[0].length;
  }
  if (last < line.length) out.push({ text: line.slice(last) });
  return out;
}

// 게시글의 코드 첨부. 6줄이 넘으면 접어 두고 눌러서 펼친다.
export default function CodeBlock({ code }) {
  const [open, setOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  const lines = code.text.split('\n');
  const long = lines.length > PREVIEW_LINES;
  const shown = long && !open ? lines.slice(0, PREVIEW_LINES) : lines;

  useEffect(() => {
    if (!copied) return undefined;
    const timer = setTimeout(() => setCopied(false), COPIED_MS);
    return () => clearTimeout(timer);
  }, [copied]);

  const copy = () => {
    navigator.clipboard?.writeText(code.text).catch(() => { /* 복사 불가 환경은 무시 */ });
    setCopied(true);
  };

  return (
    <div className="code-block">
      <div className="code-block__head">
        <Icon name="code" size={16} />
        <strong className="code-block__file">{code.file}</strong>
        <span className="code-block__lang">{code.lang}</span>
        <span className="xs muted">{lines.length}줄</span>
        <button type="button" className="code-block__copy" aria-label="코드 복사" onClick={copy}>
          <Icon name="copy" size={14} />{copied ? '복사됨' : '복사'}
        </button>
      </div>
      <div className="code-block__view">
        <div className="code-block__lines">
          {shown.map((line, i) => (
            <div key={i} className="code-block__line">
              <span className="code-block__no" aria-hidden="true">{i + 1}</span>
              <span className="code-block__text">
                {highlight(line).map((t, j) => <span key={j} className={t.kind ? `tk-${t.kind}` : undefined}>{t.text}</span>)}
              </span>
            </div>
          ))}
        </div>
        {long && !open && <div className="code-block__fade" aria-hidden="true" />}
      </div>
      {long && (
        <button type="button" className="code-block__toggle" aria-expanded={open} onClick={() => setOpen((v) => !v)}>
          <Icon name="chevronDown" size={16} className={`code-block__chevron${open ? ' is-open' : ''}`} />
          {open ? '접기' : `전체 코드 보기 · ${lines.length}줄`}
        </button>
      )}
    </div>
  );
}
