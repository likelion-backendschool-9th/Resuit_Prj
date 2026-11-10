import { forwardRef, useEffect, useId, useRef, useState } from 'react';
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

// 글 종류. 홈 상단은 피드 탭이 정하고, 게시글 작성 모달에서는 이 버튼으로 고른다.
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
const MAX_TECH_TAGS = 3;
const MAX_HASHTAGS = 10;
const HASHTAG_PATTERN = /^[\p{L}\p{N}_-]{1,50}$/u;

function normalizeHashtag(value) {
  let tag = String(value || '').trim();
  if (tag.startsWith('#')) tag = tag.slice(1).trim();
  return tag;
}

// 글쓰기 폼. 홈 상단은 피드 탭이 글 종류(kind)를 넘겨 주고, 게시글 작성 모달은 넘기지 않아 폼에서 직접 고른다.
// 글 수정은 원래 글의 종류를 그대로 쓴다. (이력서 피드백 요청은 이력서 › 내 이력서에서 한다)
const PostForm = forwardRef(function PostForm({ post, kind: lockedKind, onSave, onCancel }, textareaRef) {
  const inputId = useId();
  const fileInput = useRef(null);
  const { loggedIn } = useAppState();
  const media = useMediaAttachments();
  const pending = useRef(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [kind, setKind] = useState(lockedKind || post?.type || 'general');
  const [initialContent] = useState(() => splitPostForEditing(post?.body || ''));
  const [text, setText] = useState(initialContent.body);
  const [techTags, setTechTags] = useState(post?.techTags || post?.tags || []);
  const [hashtags, setHashtags] = useState(post?.hashtags || []);
  const [hashOpen, setHashOpen] = useState((post?.hashtags || []).length > 0);
  const [hashDraft, setHashDraft] = useState('');
  const [source, setSource] = useState('');
  const [codeOpen, setCodeOpen] = useState(!!initialContent.code);
  const [codeFile, setCodeFile] = useState(initialContent.code?.file || '');
  const [codeText, setCodeText] = useState(initialContent.code?.text || '');

  useEffect(() => {
    if (lockedKind) setKind(lockedKind);
  }, [lockedKind]);

  // 글 종류를 넘겨받지 않은 새 글(게시글 작성 모달)일 때만 종류 선택 버튼을 보여 준다.
  const choosesKind = !lockedKind && !post;
  const hasCode = codeOpen && codeText.trim().length > 0;
  const blank = !text.trim() && !hasCode && !media.items.length && !post?.image && !post?.video;

  const toggleTechTag = (t) =>
    setTechTags((list) => (list.includes(t) ? list.filter((x) => x !== t) : list.length < MAX_TECH_TAGS ? [...list, t] : list));

  const addHashtag = () => {
    const tag = normalizeHashtag(hashDraft);
    if (!tag) return;
    if (!HASHTAG_PATTERN.test(tag)) {
      setError('해시태그는 한글·영문·숫자·_·- 만 쓸 수 있어요.');
      return;
    }
    setError('');
    setHashtags((list) => {
      if (list.includes(tag) || list.length >= MAX_HASHTAGS) return list;
      return [...list, tag];
    });
    setHashDraft('');
  };

  const submit = async (event) => {
    event.preventDefault();
    if (blank || pending.current || media.busy || !media.beginPublish()) return;
    pending.current = true;
    setSaving(true);
    setError('');
    try {
      await onSave({
        type: kind,
        techTags: kind === 'tech' ? techTags : null,
        hashtags,
        content: serializePostContent(text, hasCode ? { file: codeFile, text: codeText } : null),
        imageUrl: post?.image || media.items.find((item) => item.mediaType === 'IMAGE')?.path || null,
        videoUrl: post?.video || media.items.find((item) => item.mediaType === 'VIDEO')?.path || null,
        repoFullName: post?.repoFullName || null,
      });
      media.finishPublish(true);
      if (!post) {
        setTechTags([]);
        setHashtags([]);
        setHashDraft('');
        setHashOpen(false);
        setText('');
        setCodeFile('');
        setCodeText('');
        setCodeOpen(false);
      }
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
        {choosesKind && <ChipRadioGroup options={KINDS} value={kind} onChange={setKind} label="글 종류" />}
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
            <span className="composer__tech-label">기술 태그 선택 (최대 {MAX_TECH_TAGS}개)</span>
            <div className="chips" style={{ gap: 6 }}>
              {TECH_TAGS.map((t) => (
                <ToggleChip key={t} tone="tech" size="filter" on={techTags.includes(t)} onClick={() => toggleTechTag(t)}>#{t}</ToggleChip>
              ))}
            </div>
            <label className="composer__source">
              출처
              <input type="url" placeholder="공식 문서나 원문 링크 (선택)" disabled title="출처 저장은 아직 지원하지 않아요." value={source} onChange={(e) => setSource(e.target.value)} />
            </label>
          </div>
        )}

        {hashOpen && (
          <div className="composer__tech">
            <span className="composer__tech-label">해시태그 (최대 {MAX_HASHTAGS}개)</span>
            <div className="chips" style={{ gap: 6 }}>
              {hashtags.map((t) => (
                <ToggleChip key={t} size="filter" on onClick={() => setHashtags((list) => list.filter((x) => x !== t))}>#{t}</ToggleChip>
              ))}
            </div>
            <label className="composer__source">
              태그 추가
              <input
                type="text"
                placeholder="#면접후기"
                value={hashDraft}
                onChange={(e) => setHashDraft(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    addHashtag();
                  }
                }}
              />
              <Button type="button" variant="outline" size="sm" onClick={addHashtag}>추가</Button>
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
            <IconButton
              label="해시태그"
              title="해시태그 추가"
              aria-pressed={hashOpen}
              className={`icon-btn--plain icon-btn--sm composer__tool${hashOpen ? ' is-on' : ''}`}
              onClick={() => setHashOpen((v) => !v)}
            >
              <Icon name="hash" />
            </IconButton>
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
