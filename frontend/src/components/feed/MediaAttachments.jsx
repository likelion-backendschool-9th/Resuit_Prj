import { IconButton } from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import './MediaAttachments.css';

export default function MediaAttachments({ media, saving }) {
  return <div className="media-attachments" aria-busy={media.busy}>
    {media.items.map((item) => <figure key={item.path} className="media-attachments__item">
      {item.mediaType === 'IMAGE'
        ? <img src={item.preview} alt={`${item.name} 첨부 미리보기`} />
        : <video src={item.preview} controls playsInline preload="metadata" aria-label={`${item.name} 첨부 미리보기`} />}
      <IconButton label={`${item.name} 첨부 취소`} className="media-attachments__remove"
        disabled={saving || media.busy} onClick={() => media.remove(item)}><Icon name="x" size={18} /></IconButton>
      <figcaption>{item.name} · 게시 대기</figcaption>
    </figure>)}
    {media.busy && <span className="small muted" role="status">첨부파일을 처리하고 있어요…</span>}
    {media.error && <p role="alert">{media.error}</p>}
  </div>;
}
