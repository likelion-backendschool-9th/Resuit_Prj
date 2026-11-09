'use client';

import { useEffect, useRef, useState } from 'react';
import Button from '@/components/common/Button.jsx';
import './CompanyVerificationCancelModal.css';

export default function CompanyVerificationCancelModal({ onClose, onConfirm }) {
  const dialog = useRef(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const element = dialog.current;
    const previousFocus = document.activeElement;
    element.showModal();
    return () => {
      if (element.open) element.close();
      previousFocus?.focus();
    };
  }, []);

  async function confirm() {
    setError('');
    setBusy(true);
    try {
      await onConfirm();
    } catch (reason) {
      setError(reason.message || '기업 이메일 인증을 취소하지 못했어요.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <dialog
      ref={dialog}
      className="company-verification-modal"
      aria-labelledby="company-verification-modal-title"
      onCancel={(event) => { event.preventDefault(); if (!busy) onClose(); }}
      onClick={(event) => event.stopPropagation()}
    >
      <div className="company-verification-modal__body">
        <h2 id="company-verification-modal-title">기업 이메일 인증을 취소할까요?</h2>
        <p>취소하면 기업 계정 권한이 사라지고, 기업 공고를 관리할 수 없게 됩니다.</p>
        {error && <p className="company-verification-modal__error" role="alert">{error}</p>}
        <div className="company-verification-modal__actions">
          <Button variant="outline" onClick={onClose} disabled={busy}>돌아가기</Button>
          <Button onClick={confirm} disabled={busy}>{busy ? '취소 중…' : '인증 취소'}</Button>
        </div>
      </div>
    </dialog>
  );
}
