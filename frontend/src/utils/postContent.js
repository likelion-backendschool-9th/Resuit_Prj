const LANGUAGES = { java: 'Java', js: 'JavaScript', javascript: 'JavaScript', jsx: 'JavaScript', ts: 'TypeScript', typescript: 'TypeScript', tsx: 'TypeScript', py: 'Python', python: 'Python', sql: 'SQL', kt: 'Kotlin', kotlin: 'Kotlin', css: 'CSS', html: 'HTML', json: 'JSON', sh: 'Shell', bash: 'Shell' };

export function parsePostContent(content = '') {
  const text = content || '';
  const blocks = [];
  const opening = /^(`{3,})([^\r\n`]*)\r?\n/gm;
  let cursor = 0;
  let match;
  while ((match = opening.exec(text))) {
    const closing = new RegExp(`^${match[1]}[ \\t]*(?=\\r?$)`, 'gm');
    closing.lastIndex = opening.lastIndex;
    const end = closing.exec(text);
    if (!end) continue;
    if (match.index > cursor) blocks.push({ type: 'text', text: text.slice(cursor, match.index) });
    const [language = '', ...metadata] = match[2].trim().split(/\s+/);
    const filename = metadata.find((value) => value.startsWith('filename='))?.slice(9);
    let file = language ? `snippet.${language}` : 'snippet.txt';
    if (filename) { try { file = decodeURIComponent(filename); } catch { /* 일반 파일명 사용 */ } }
    blocks.push({ type: 'code', start: match.index, end: closing.lastIndex, code: {
      file, lang: LANGUAGES[language.toLowerCase()] || language || 'Text',
      text: text.slice(opening.lastIndex, end.index).replace(/\r?\n$/, ''),
    } });
    cursor = closing.lastIndex;
    opening.lastIndex = cursor;
  }
  if (cursor < text.length) blocks.push({ type: 'text', text: text.slice(cursor) });
  return blocks;
}

export function splitPostForEditing(content = '') {
  const blocks = parsePostContent(content);
  const code = blocks.filter((block) => block.type === 'code').at(-1);
  // 마지막 코드 영역만 전용 입력란으로 분리하고 나머지 원문은 그대로 보존한다.
  if (!code || content.slice(code.end).trim()) return { body: content, code: null };
  return { body: content.slice(0, code.start).replace(/\n\n$/, ''), code: code.code };
}

export function serializePostContent(body, code) {
  if (!code?.text.trim()) return body;
  const file = code.file.trim() || 'snippet.txt';
  const extension = file.split('.').pop().toLowerCase();
  const language = /^[a-z0-9]+$/.test(extension) ? extension : 'text';
  // 코드 안에 백틱이 포함되어도 블록이 조기에 닫히지 않게 더 긴 구분자를 쓴다.
  const longest = Math.max(2, ...(code.text.match(/`+/g) || []).map((run) => run.length));
  const fence = '`'.repeat(longest + 1);
  return `${body}${body ? '\n\n' : ''}${fence}${language} filename=${encodeURIComponent(file)}\n${code.text}\n${fence}`;
}
