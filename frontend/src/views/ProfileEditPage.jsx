'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Button from '../components/common/Button.jsx';
import useProfileData from '../components/profile/useProfileData.js';
import { profileApi } from '../services/profileApi.js';
import { REGIONS } from '../data/constants.js';
import './ProfileEditPage.css';

function nextCareerKey() {
  return `career-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

function blankCareer() {
  return {
    key: nextCareerKey(),
    mode: 'write',
    fresh: true,
    companyName: '',
    jobTitle: '',
    startDate: '',
    endDate: '',
    description: '',
  };
}

function careerFromProfile(item) {
  return {
    key: `saved-${item.id}`,
    mode: 'read',
    fresh: false,
    companyName: item.companyName || '',
    jobTitle: item.jobTitle || '',
    startDate: item.startDate || '',
    endDate: item.endDate || '',
    description: item.description || '',
  };
}

function careerPeriod(item) {
  const start = (item.startDate || '').replaceAll('-', '.');
  const end = item.endDate ? item.endDate.replaceAll('-', '.') : '재직 중';
  return start ? `${start} ~ ${end}` : end;
}

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
  const [region, setRegion] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [pendingFile, setPendingFile] = useState(null);
  const [localPreview, setLocalPreview] = useState('');
  const [careers, setCareers] = useState([]);
  const [message, setMessage] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!data.profile) return;
    setNickname(data.profile.nickname || '');
    setIntro(data.profile.selfIntroduction || '');
    setRegion(data.profile.region || '');
    setImageUrl(data.profile.profileImageUrl || '');
    setCareers((data.profile.careers || []).map(careerFromProfile));
    setPendingFile(null);
  }, [data.profile]);

  useEffect(() => () => {
    if (localPreview) URL.revokeObjectURL(localPreview);
  }, [localPreview]);

  const updateCareer = (key, patch) => {
    setCareers((rows) => rows.map((row) => (row.key === key ? { ...row, ...patch } : row)));
  };

  const beginCareer = () => {
    if (careers.some((item) => item.mode === 'write')) {
      setMessage('작성 중인 경력을 추가한 뒤 다음 경력을 적어 주세요.');
      return;
    }
    setMessage('');
    setCareers((rows) => [...rows, blankCareer()]);
  };

  const confirmCareer = (key) => {
    const item = careers.find((row) => row.key === key);
    if (!item) return;
    if (!item.companyName.trim() || !item.jobTitle.trim() || !item.startDate) {
      setMessage('회사명, 직무, 시작일을 입력한 뒤 추가해 주세요.');
      return;
    }
    if (item.endDate && item.endDate < item.startDate) {
      setMessage('종료일은 시작일보다 빠를 수 없습니다.');
      return;
    }
    setMessage('');
    updateCareer(key, { mode: 'read', fresh: false, snapshot: undefined });
  };

  const editCareer = (key) => {
    if (careers.some((item) => item.mode === 'write' && item.key !== key)) {
      setMessage('작성 중인 경력을 추가한 뒤 다른 경력을 수정해 주세요.');
      return;
    }
    setMessage('');
    setCareers((rows) => rows.map((row) => (
      row.key === key
        ? {
          ...row,
          mode: 'write',
          snapshot: {
            companyName: row.companyName,
            jobTitle: row.jobTitle,
            startDate: row.startDate,
            endDate: row.endDate,
            description: row.description,
          },
        }
        : row
    )));
  };

  const cancelCareer = (key) => {
    setMessage('');
    setCareers((rows) => rows.flatMap((row) => {
      if (row.key !== key) return [row];
      if (row.fresh || !row.snapshot) return [];
      return [{
        ...row,
        ...row.snapshot,
        mode: 'read',
        snapshot: undefined,
      }];
    }));
  };

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
      const draft = careers.find((item) => item.mode === 'write' && (
        item.companyName.trim() || item.jobTitle.trim() || item.startDate || item.endDate || item.description.trim()
      ));
      if (draft) {
        throw new Error('작성 중인 경력을 추가한 뒤 저장해 주세요.');
      }
      await data.updateProfile({
        nickname: nickname.trim() || null,
        selfIntroduction: intro.trim(),
        // 고르지 않았으면 보내지 않아 서버가 기존 값을 유지한다.
        region: region || null,
        profileImageUrl: nextImage || '',
        careers: careers
          .filter((item) => item.mode === 'read')
          .map((item) => ({
            companyName: item.companyName.trim(),
            jobTitle: item.jobTitle.trim(),
            startDate: item.startDate || null,
            endDate: item.endDate || null,
            description: item.description.trim() || null,
          })),
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
            <label className="profile-edit__field">
              지역
              <select
                value={region}
                disabled={!loggedIn || data.loading}
                onChange={(event) => setRegion(event.target.value)}
              >
                {!region && <option value="">선택해 주세요</option>}
                {REGIONS.map((item) => <option key={item.id} value={item.id}>{item.label}</option>)}
              </select>
              <span className="small muted">기업 회원이 인재 찾기에서 지역으로 찾을 때 쓰여요.</span>
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
            <div className="profile-edit__field">
              <span>경력</span>
              <p className="small muted">경력 추가로 입력칸을 연 뒤, 추가를 누르면 읽기 카드로 바뀝니다. 종료일을 비우면 재직 중입니다.</p>
              {careers.map((item) => (
                item.mode === 'read' ? (
                  <article key={item.key} className="profile-edit__career is-read">
                    <strong>{item.companyName}</strong>
                    <span className="profile-edit__career-role">{item.jobTitle}</span>
                    <span className="small muted">{careerPeriod(item)}</span>
                    {item.description && <p className="small">{item.description}</p>}
                    <div className="profile-edit__actions">
                      <Button type="button" variant="outline" size="sm" disabled={!loggedIn || saving} onClick={() => editCareer(item.key)}>
                        수정
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        disabled={!loggedIn || saving}
                        onClick={() => setCareers((rows) => rows.filter((row) => row.key !== item.key))}
                      >
                        삭제
                      </Button>
                    </div>
                  </article>
                ) : (
                  <div key={item.key} className="profile-edit__career">
                    <label className="profile-edit__field">
                      회사명
                      <input
                        value={item.companyName}
                        maxLength={100}
                        disabled={!loggedIn || data.loading}
                        onChange={(event) => updateCareer(item.key, { companyName: event.target.value })}
                      />
                    </label>
                    <label className="profile-edit__field">
                      직무
                      <input
                        value={item.jobTitle}
                        maxLength={100}
                        disabled={!loggedIn || data.loading}
                        onChange={(event) => updateCareer(item.key, { jobTitle: event.target.value })}
                      />
                    </label>
                    <div className="profile-edit__career-dates">
                      <label className="profile-edit__field">
                        시작일
                        <input
                          type="date"
                          value={item.startDate}
                          disabled={!loggedIn || data.loading}
                          onChange={(event) => updateCareer(item.key, { startDate: event.target.value })}
                        />
                      </label>
                      <label className="profile-edit__field">
                        종료일
                        <input
                          type="date"
                          value={item.endDate}
                          disabled={!loggedIn || data.loading}
                          onChange={(event) => updateCareer(item.key, { endDate: event.target.value })}
                        />
                      </label>
                    </div>
                    <label className="profile-edit__field">
                      설명
                      <textarea
                        value={item.description}
                        maxLength={1000}
                        rows={2}
                        disabled={!loggedIn || data.loading}
                        onChange={(event) => updateCareer(item.key, { description: event.target.value })}
                        placeholder="맡은 일을 짧게 적어도 됩니다"
                      />
                    </label>
                    <div className="profile-edit__actions">
                      <Button type="button" size="sm" disabled={!loggedIn || saving} onClick={() => confirmCareer(item.key)}>
                        {item.fresh ? '추가' : '완료'}
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        disabled={!loggedIn || saving}
                        onClick={() => cancelCareer(item.key)}
                      >
                        취소
                      </Button>
                    </div>
                  </div>
                )
              ))}
              <Button
                type="button"
                variant="outline"
                disabled={!loggedIn || data.loading || careers.length >= 20}
                onClick={beginCareer}
              >
                경력 추가
              </Button>
            </div>
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
