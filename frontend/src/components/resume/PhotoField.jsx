'use client';

import { useEffect, useState } from 'react';
import Button from '../common/Button.jsx';
import { IMAGE_ACCEPT, discardImage, uploadImage } from '../../services/images.js';

// 이력서 사진 칸. 공통·특화 이력서 편집이 함께 쓴다.
// value: ''(사진 없음) | '/media/image/...'(저장된 사진) | 'tmpImage/...'(방금 올린 사진, 이력서를 저장해야 확정)
export default function PhotoField({ value = '', onChange, disabled }) {
  // 방금 올린 사진은 서버 주소가 아직 없어 내 컴퓨터 파일로 미리 보여 준다.
  const [preview, setPreview] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => () => { if (preview) URL.revokeObjectURL(preview); }, [preview]);

  const src = value.startsWith('/media/') ? value : (value ? preview : '');

  async function pick(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    setBusy(true); setError('');
    try {
      const uploaded = await uploadImage(file);
      discardImage(value);
      setPreview(URL.createObjectURL(file));
      onChange(uploaded.path);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  function remove() {
    discardImage(value);
    setPreview('');
    onChange('');
  }

  return (
    <div className="resume-photo">
      <div className="resume-photo__frame" aria-hidden={!src}>
        {src ? <img src={src} alt="이력서 사진" /> : <span className="xs muted">사진 없음</span>}
      </div>
      <div className="resume-photo__body">
        <span className="resume-photo__label">이력서 사진</span>
        <span className="xs muted">JPG·PNG·GIF·WebP, 5MB 이하. 이력서를 저장해야 반영돼요.</span>
        <div className="row-wrap" style={{ gap: 8 }}>
          <label className={`btn btn--outline btn--sm resume-photo__pick${disabled || busy ? ' is-disabled' : ''}`}>
            {busy ? '올리는 중…' : src ? '사진 바꾸기' : '사진 올리기'}
            <input type="file" accept={IMAGE_ACCEPT} onChange={pick} disabled={disabled || busy} />
          </label>
          {src && <Button type="button" variant="ghost" size="sm" onClick={remove} disabled={disabled || busy}>사진 빼기</Button>}
        </div>
        {error && <p role="alert" className="small resume-photo__error">{error}</p>}
      </div>
    </div>
  );
}
