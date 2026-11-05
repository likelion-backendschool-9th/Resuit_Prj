import { forwardRef, useId, useRef, useState } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import { MEDIA_ACCEPT } from '../../services/media.js';
import useMediaAttachments from './useMediaAttachments.js';
import MediaAttachments from './MediaAttachments.jsx';
import { splitPostForEditing, serializePostContent } from '../../utils/postContent.js';
import { TECH_TAGS } from '../../data/constants.js';
import Button, { IconButton } from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import { ChipRadioGroup, ToggleChip } from '../common/Chip.jsx';
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
const MAX_TAGS = 3;

// 홈 피드 상단 글쓰기. (이력서 피드백 요청은 이력서 › 내 이력서에서 한다)
const PostForm = forwardRef(function PostForm({ post, onSave, onCancel }, textareaRef) {
  const inputId = useId();
  const fileInput = useRef(null);
  const { loggedIn } = useAppState();
  const media = useMediaAttachments();
  const pending = useRef(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [kind, setKind] = useState(post?.type || 'general');
  const [initialContent] = useState(() => splitPostForEditing(post?.body || ''));
  const [text, setText] = useState(initialContent.body);
  const [tags, setTags] = useState([]);
  const [source, setSource] = useState('');
  const [codeOpen, setCodeOpen] = useState(!!initialContent.code);
  const [codeFile, setCodeFile] = useState(initialContent.code?.file || '');
  const [codeText, setCodeText] = useState(initialContent.code?.text || '');

  const hasCode = codeOpen && codeText.trim().length > 0;
  const blank = !text.trim() && !hasCode && !media.items.length && !post?.image && !post?.video;

  const toggleTag = (t) =>
    setTags((list) => (list.includes(t) ? list.filter((x) => x !== t) : list.length < MAX_TAGS ? [...list, t] : list));

  const submit = async (event) => {
    event.preventDefault();
    if (blank || pending.current || media.busy || !media.beginPublish()) return;
    pending.current = true;
    setSaving(true);
    setError('');
    try {
      await onSave({ type: kind, content: serializePostContent(text, hasCode ? { file: codeFile, text: codeText } : null), imageUrl: post?.image || media.items.find((item) => item.mediaType === 'IMAGE')?.path || null,
        videoUrl: post?.video || media.items.find((item) => item.mediaType === 'VIDEO')?.path || null, repoFullName: post?.repoFullName || null });
      media.finishPublish(true);
      if (!post) { setText(''); setCodeFile(''); setCodeText(''); setCodeOpen(false); }
    } catch (err) {
      media.finishPublish(false);
      setError(err.message || '저장하지 못했어요. 다시 시도해 주세요.');
    } finally {
      pending.current = false;
      setSaving(false);
    }
  };

  return (
    <form onSubmit={submit} aria-busy={saving}>
      <fieldset className="composer__fields composer__body" disabled={saving}>
        <ChipRadioGroup options={KINDS.some((item) => item.id === kind) ? KINDS : [...KINDS, { id: kind, label: kind }]} value={kind} onChange={setKind} label="글 종류" />
        <label htmlFor={inputId} className="sr-only">새 게시글</label>
        <textarea
          id={inputId}
          ref={textareaRef}
          className="composer__text"
          rows={3}
          placeholder={PLACEHOLDERS[kind]}
          value={text}
          onChange={(e) => setText(e.target.value)}
        />

        {kind === 'tech' && (
          <div className="composer__tech">
            <span className="composer__tech-label">기술 태그 선택 (최대 {MAX_TAGS}개)</span>
            <fieldset disabled className="composer__unsupported" title="기술 태그 저장은 아직 지원하지 않아요.">
            <div className="chips" style={{ gap: 6 }}>
              {TECH_TAGS.map((t) => (
                <ToggleChip key={t} tone="tech" size="filter" on={tags.includes(t)} onClick={() => toggleTag(t)}>#{t}</ToggleChip>
              ))}
            </div>
            </fieldset>
            <label className="composer__source">
              출처
              <input type="url" placeholder="공식 문서나 원문 링크 (선택)" disabled title="출처 저장은 아직 지원하지 않아요." value={source} onChange={(e) => setSource(e.target.value)} />
            </label>
          </div>
        )}

        {codeOpen && (
          <div className="composer__code">
            <div className="composer__code-head">
              <Icon name="code" size={16} />
              <label htmlFor={`${inputId}-code-file`} className="sr-only">파일 이름</label>
              <input id={`${inputId}-code-file`} className="composer__code-file" placeholder="파일 이름 (예: UserService.java)" value={codeFile} onChange={(e) => setCodeFile(e.target.value)} />
              <span className="xs muted">{codeText ? codeText.split('\n').length : 0}줄</span>
            </div>
            <label htmlFor={`${inputId}-code-text`} className="sr-only">코드</label>
            <textarea
              id={`${inputId}-code-text`}
              className="composer__code-text"
              rows={6}
              spellCheck={false}
              placeholder="코드를 붙여 넣으세요. 게시하면 6줄까지만 보이고, 눌러서 전체를 펼쳐요."
              value={codeText}
              onChange={(e) => setCodeText(e.target.value)}
            />
          </div>
        )}

        {!post && <MediaAttachments media={media} saving={saving} />}
        <input ref={fileInput} type="file" accept={MEDIA_ACCEPT} hidden onChange={(event) => {
          const file = event.target.files?.[0];
          event.target.value = '';
          media.add(file);
        }} />
        <div className="composer__foot">
          <div className="row" style={{ gap: 4 }}>
            <IconButton label="이미지 또는 동영상 첨부" disabled={!!post || media.busy || !loggedIn}
              title={post ? '수정할 때는 기존 첨부를 유지합니다.' : !loggedIn ? '로그인 후 첨부할 수 있어요.' : '이미지·영상 각각 1개, 파일당 최대 50MB'}
              onClick={() => fileInput.current?.click()} className="icon-btn--plain icon-btn--sm composer__tool"><Icon name="image" /></IconButton>
            <IconButton
              label="코드 블록" title="코드 첨부 또는 제거"
              aria-pressed={codeOpen}
              className={`icon-btn--plain icon-btn--sm composer__tool${codeOpen ? ' is-on' : ''}`}
              onClick={() => setCodeOpen((v) => !v)}
            >
              <Icon name="code" />
            </IconButton>
            <IconButton label="해시태그" disabled title="해시태그 저장은 아직 지원하지 않아요." className="icon-btn--plain icon-btn--sm composer__tool"><Icon name="hash" /></IconButton>
          </div>
          <div className="row" style={{ gap: 12 }}>
            <span className="small muted">{text.length}자</span>
            {onCancel && <Button variant="ghost" onClick={onCancel}>취소</Button>}
            <Button type="submit" disabled={blank || media.busy}>{saving ? '저장 중…' : post ? '수정 저장' : '게시'}</Button>
          </div>
        </div>
      </fieldset>
      {error && <p role="alert">{error}</p>}
    </form>
  );
});

export default PostForm;
