import { forwardRef, useState } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import { TECH_TAGS } from '../../data/mock.js';
import Avatar from '../common/Avatar.jsx';
import Button, { IconButton } from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import { ChipRadioGroup, ToggleChip } from '../common/Chip.jsx';
import { langFromFile } from './CodeBlock.jsx';
import './Composer.css';

const KINDS = [
  { id: 'general', label: '일반' },
  { id: 'tech', label: '기술 정보' },
  { id: 'question', label: '질문' }
];
const PLACEHOLDERS = {
  general: '무슨 일이 있었나요? 면접 후기, 공부 기록 모두 좋아요.',
  tech: '새로 알게 된 기술, 버전 변경점, 꿀팁을 짧게 공유해 주세요.',
  question: '취업, 이력서, 개발에 대해 궁금한 걸 물어보세요.'
};
const MAX_LENGTH = 280;
const MAX_TAGS = 3;

// 홈 피드 상단 글쓰기. (이력서 피드백 요청은 이력서 › 내 이력서에서 한다)
const Composer = forwardRef(function Composer({ onPosted }, textareaRef) {
  const { me, addPost } = useAppState();
  const [kind, setKind] = useState('general');
  const [text, setText] = useState('');
  const [tags, setTags] = useState([]);
  const [source, setSource] = useState('');
  const [codeOpen, setCodeOpen] = useState(false);
  const [codeFile, setCodeFile] = useState('');
  const [codeText, setCodeText] = useState('');

  const hasCode = codeOpen && codeText.trim().length > 0;
  const blank = !text.trim() && !hasCode;

  const toggleTag = (t) =>
    setTags((list) => (list.includes(t) ? list.filter((x) => x !== t) : list.length < MAX_TAGS ? [...list, t] : list));

  const submit = () => {
    if (blank) return;
    const file = codeFile.trim() || 'snippet.txt';
    addPost({
      type: kind,
      body: text.trim(),
      tags: kind === 'tech' ? tags : [],
      source: kind === 'tech' ? source.trim() : '',
      code: hasCode ? { file, lang: langFromFile(file), text: codeText.replace(/\s+$/, '') } : null
    });
    setText('');
    setTags([]);
    setSource('');
    setCodeOpen(false);
    setCodeFile('');
    setCodeText('');
    onPosted?.();
  };

  return (
    <div className="composer" id="compose">
      <Avatar initial={me.initial} color={me.av} />
      <div className="composer__body">
        <ChipRadioGroup options={KINDS} value={kind} onChange={setKind} label="글 종류" />
        <label htmlFor="composer-text" className="sr-only">새 게시글</label>
        <textarea
          id="composer-text"
          ref={textareaRef}
          className="composer__text"
          rows={3}
          maxLength={MAX_LENGTH}
          placeholder={PLACEHOLDERS[kind]}
          value={text}
          onChange={(e) => setText(e.target.value)}
        />

        {kind === 'tech' && (
          <div className="composer__tech">
            <span className="composer__tech-label">기술 태그 선택 (최대 {MAX_TAGS}개)</span>
            <div className="chips" style={{ gap: 6 }}>
              {TECH_TAGS.map((t) => (
                <ToggleChip key={t} tone="tech" size="filter" on={tags.includes(t)} onClick={() => toggleTag(t)}>#{t}</ToggleChip>
              ))}
            </div>
            <label className="composer__source">
              출처
              <input type="url" placeholder="공식 문서나 원문 링크 (선택)" value={source} onChange={(e) => setSource(e.target.value)} />
            </label>
          </div>
        )}

        {codeOpen && (
          <div className="composer__code">
            <div className="composer__code-head">
              <Icon name="code" size={16} />
              <label htmlFor="composer-code-file" className="sr-only">파일 이름</label>
              <input id="composer-code-file" className="composer__code-file" placeholder="파일 이름 (예: UserService.java)" value={codeFile} onChange={(e) => setCodeFile(e.target.value)} />
              <span className="xs muted">{codeText ? codeText.split('\n').length : 0}줄</span>
            </div>
            <label htmlFor="composer-code-text" className="sr-only">코드</label>
            <textarea
              id="composer-code-text"
              className="composer__code-text"
              rows={6}
              spellCheck={false}
              placeholder="코드를 붙여 넣으세요. 게시하면 6줄까지만 보이고, 눌러서 전체를 펼쳐요."
              value={codeText}
              onChange={(e) => setCodeText(e.target.value)}
            />
          </div>
        )}

        <div className="composer__foot">
          <div className="row" style={{ gap: 4 }}>
            <IconButton label="이미지 첨부" className="icon-btn--plain icon-btn--sm composer__tool"><Icon name="image" /></IconButton>
            <IconButton
              label="코드 블록"
              aria-pressed={codeOpen}
              className={`icon-btn--plain icon-btn--sm composer__tool${codeOpen ? ' is-on' : ''}`}
              onClick={() => setCodeOpen((v) => !v)}
            >
              <Icon name="code" />
            </IconButton>
            <IconButton label="해시태그" className="icon-btn--plain icon-btn--sm composer__tool"><Icon name="hash" /></IconButton>
          </div>
          <div className="row" style={{ gap: 12 }}>
            <span className="small muted">{text.length} / {MAX_LENGTH}</span>
            <Button onClick={submit} disabled={blank}>게시</Button>
          </div>
        </div>
      </div>
    </div>
  );
});

export default Composer;
