'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Button from '../components/common/Button.jsx';
import useProfileData from '../components/profile/useProfileData.js';
import { profileApi } from '../services/profileApi.js';
import './ProfileEditPage.css';

function previewSrc(imageUrl, localPreview) {
  if (localPreview) return localPreview;
  const value = (imageUrl || '').trim();
  if (!value) return undefined;
  if (value.startsWith('https://') || value.startsWith('/media/')) return value;
  return undefined;
}

export default function ProfileEditPage() {
  const { me, loggedIn, authLoading, refreshSession } = useAppState();
  const data = useProfileData(me.handle, true, null, 'posts', me.handle, {
    ready: !authLoading && loggedIn,
  });
  const [nickname, setNickname] = useState('');
  const [intro, setIntro] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [pendingFile, setPendingFile] = useState(null);
  const [localPreview, setLocalPreview] = useState('');
  const [message, setMessage] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!data.profile) return;
    setNickname(data.profile.nickname || '');
    setIntro(data.profile.selfIntroduction || '');
    setImageUrl(data.profile.profileImageUrl || '');
    setPendingFile(null);
  }, [data.profile]);

  useEffect(() => () => {
    if (localPreview) URL.revokeObjectURL(localPreview);
  }, [localPreview]);

  const previewName = nickname.trim() || data.profile?.nickname || me.name || '?';
  const avatarSrc = previewSrc(imageUrl, localPreview);

  const onPickFile = (event) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    setMessage('');
    if (localPreview) URL.revokeObjectURL(localPreview);
    setPendingFile(file);
    setLocalPreview(URL.createObjectURL(file));
  };

  const clearImage = () => {
    if (localPreview) URL.revokeObjectURL(localPreview);
    setPendingFile(null);
    setLocalPreview('');
    setImageUrl('');
    setMessage('');
  };

  const save = async () => {
    setMessage('');
    setSaving(true);
    try {
      let nextImage = imageUrl.trim();
      if (pendingFile) {
        nextImage = await profileApi.uploadProfileImage(pendingFile);
      } else if (nextImage && !/^https:\/\//i.test(nextImage) && !nextImage.startsWith('/media/')) {
        throw new Error('프로필 사진은 파일을 고르거나 https:// 주소를 입력해 주세요.');
      }
      await data.updateProfile({
        nickname: nickname.trim() || null,
        selfIntroduction: intro.trim(),
        profileImageUrl: nextImage || '',
      });
      if (localPreview) URL.revokeObjectURL(localPreview);
      setPendingFile(null);
      setLocalPreview('');
      await refreshSession?.();
      setMessage('프로필을 저장했습니다.');
    } catch (err) {
      setMessage(err?.message || '프로필을 저장하지 못했습니다.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <MainColumn>
        <div className="page-title"><h1>프로필 편집</h1></div>
        <div className="profile-edit__content">
          {authLoading && <p className="small muted">로그인 상태를 확인하는 중…</p>}
          {!loggedIn && !authLoading && (
            <p className="small muted">로그인하면 닉네임·소개·프로필 사진을 수정할 수 있어요.</p>
          )}
          {message && (
            <p role="status" className="small" style={{ color: 'var(--accent)' }}>
              {message}
            </p>
          )}
          {data.error && <p role="alert" className="small muted">{data.error}</p>}

          <section className="card profile-edit__card">
            <h2>미리보기</h2>
            <div className="profile-edit__preview">
              <Avatar
                initial={previewName.slice(0, 1)}
                color={me.av}
                size={72}
                src={avatarSrc}
              />
              <div className="profile-edit__preview-meta">
                <strong>{previewName}</strong>
                <span className="small muted">@{data.profile?.username || me.handle}</span>
              </div>
            </div>
          </section>

          <section className="card profile-edit__card">
            <h2>기본 정보</h2>
            <p className="small muted">
              프로필 주소(@아이디)는 바꾸지 않습니다. 화면에 보이는 이름·소개·사진만 수정합니다.
            </p>
            <label className="profile-edit__field">
              닉네임
              <input
                value={nickname}
                maxLength={50}
                disabled={!loggedIn || data.loading}
                onChange={(event) => setNickname(event.target.value)}
                placeholder="프로필에 보일 이름"
              />
            </label>
            <label className="profile-edit__field">
              자기소개
              <textarea
                value={intro}
                maxLength={500}
                rows={4}
                disabled={!loggedIn || data.loading}
                onChange={(event) => setIntro(event.target.value)}
                placeholder="간단한 소개를 적어 보세요"
              />
            </label>
            <div className="profile-edit__field">
              <span>프로필 사진</span>
              <p className="small muted">jpg · png · gif · webp, 최대 5MB. 저장할 때 서버에 올립니다.</p>
              <div className="profile-edit__actions">
                <label className="profile-edit__file-btn">
                  파일 선택
                  <input
                    type="file"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                    disabled={!loggedIn || data.loading || saving}
                    onChange={onPickFile}
                  />
                </label>
                {(imageUrl || pendingFile) && (
                  <Button type="button" variant="outline" onClick={clearImage} disabled={saving}>
                    사진 제거
                  </Button>
                )}
              </div>
              {pendingFile && (
                <p className="small muted">선택됨: {pendingFile.name}</p>
              )}
            </div>
            <label className="profile-edit__field">
              또는 HTTPS 이미지 주소
              <input
                value={pendingFile ? '' : imageUrl}
                maxLength={500}
                disabled={!loggedIn || data.loading || Boolean(pendingFile)}
                onChange={(event) => setImageUrl(event.target.value)}
                placeholder="https://..."
              />
            </label>
            <div className="profile-edit__actions">
              <Button onClick={save} disabled={!loggedIn || saving || data.loading}>
                {saving ? '저장 중…' : '저장'}
              </Button>
              <Button to="/profile" variant="outline">프로필 보기</Button>
            </div>
          </section>
        </div>
      </MainColumn>
      <RightAside>
        <section className="card">
          <h2>다른 설정</h2>
          <Link href="/setting/integrations" className="text-link">연동 관리 (GitHub · 블로그)</Link>
        </section>
      </RightAside>
    </>
  );
}
