'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Button from '../components/common/Button.jsx';
import { ToggleChip } from '../components/common/Chip.jsx';
import useProfileData from '../components/profile/useProfileData.js';
import { startSocialLink, unlinkSocialProvider } from '../services/auth.js';
import './IntegrationsPage.css';
import PasswordSetupForm from '@/components/auth/PasswordSetupForm.jsx';

const MAX_PINNED_REPOS = 3;
async function copyVerificationCode(code) {
  if (navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(code);
      return;
    } catch {
      // HTTP 환경이나 브라우저 권한 제한 시 기존 복사 방식으로 시도한다.
    }
  }

  const input = document.createElement('textarea');
  input.value = code;
  input.setAttribute('readonly', '');
  input.style.position = 'fixed';
  input.style.left = '-9999px';
  document.body.appendChild(input);
  let copied = false;
  try {
    input.focus();
    input.select();
    copied = document.execCommand('copy');
  } catch {
    // 브라우저에서 대체 복사도 차단할 수 있다.
  } finally {
    input.remove();
  }
  if (!copied) {
    throw new Error('복사할 수 없습니다. 위 검증 코드를 직접 선택해 복사해 주세요.');
  }
}

const PROVIDER_NAMES = { google: 'Google', github: 'GitHub', naver: '네이버' };
const LINK_ERRORS = {
  already_linked: '이미 다른 Resuit 계정에 연결된 소셜 계정입니다. 기존 계정과 데이터는 유지되며, 이 계정으로 옮겨지지 않습니다.',
  provider_already_connected: '이미 이 제공자가 연결되어 있습니다.',
  link_provider_mismatch: '연결하려는 제공자와 로그인 제공자가 다릅니다.',
  link_user_missing: '연결할 로그인 세션을 확인할 수 없습니다. 다시 로그인해 주세요.',
  link_failed: '계정 연결에 실패했습니다. 다시 시도해 주세요.',
  tistory_unconfigured: '티스토리 앱 키가 설정되지 않았습니다.',
  tistory_denied: '티스토리 연결을 취소했습니다.',
  tistory_failed: '티스토리 연결에 실패했습니다. 다시 시도해 주세요.',
  tistory_state: '티스토리 연결 시간이 지났습니다. 다시 시도해 주세요.',
  tistory_rss: '티스토리 RSS를 읽을 수 없습니다. 블로그가 공개인지 확인해 주세요.',
};

export default function IntegrationsPage() {
  const {
    me, loggedIn, authLoading, authProviders, linkedProviders, passwordLoginEnabled, refreshSession,
  } = useAppState();
  const data = useProfileData(me.handle, true, null, 'posts', me.handle, {
    ready: !authLoading && loggedIn,
  });
  const github = data.integrations?.find((item) => item.provider === 'GITHUB' && item.connected);
  const [platform, setPlatform] = useState('VELOG');
  const [blogId, setBlogId] = useState('');
  const [rssUrl, setRssUrl] = useState('');
  const [blogVerify, setBlogVerify] = useState(null);
  const [selectedRepos, setSelectedRepos] = useState(() => (
    (data.repos || [])
      .map((repo) => repo.fullName)
      .filter(Boolean)
      .slice(0, MAX_PINNED_REPOS)
  ));
  const [message, setMessage] = useState('');
  const blog = data.integrations?.find((item) => item.provider === 'BLOG');
  const pickerRepos = data.candidateRepos || [];
  const selectedKey = (data.repos || []).map((repo) => repo.fullName).filter(Boolean).join('\n');
  const linkedSet = new Set(linkedProviders || []);

  useEffect(() => {
    if (!data.repos) return;
    setSelectedRepos(
      data.repos.map((repo) => repo.fullName).filter(Boolean).slice(0, MAX_PINNED_REPOS),
    );
  }, [selectedKey, data.repos]);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    let linked = params.get('linked');
    let error = params.get('error');
    if (linked || error) {
      sessionStorage.setItem('resuit-link-result', JSON.stringify({ linked, error }));
      window.history.replaceState({}, '', window.location.pathname);
    } else {
      const saved = sessionStorage.getItem('resuit-link-result');
      if (!saved) return;
      sessionStorage.removeItem('resuit-link-result');
      try {
        const parsed = JSON.parse(saved);
        linked = parsed.linked;
        error = parsed.error;
      } catch {
        return;
      }
    }
    if (linked) {
      setMessage(linked === 'tistory'
        ? '티스토리 블로그를 연결했습니다.'
        : `${PROVIDER_NAMES[linked] || linked} 계정을 연결했습니다.`);
      refreshSession?.();
    } else if (error) {
      setMessage(LINK_ERRORS[error] || LINK_ERRORS.link_failed);
    }
  }, [refreshSession]);

  const run = async (action, success) => {
    setMessage('');
    try {
      await action();
      setMessage(success);
    } catch (err) {
      setMessage(err?.message || '요청을 처리하지 못했습니다. 연결 상태를 확인해 주세요.');
    }
  };

  const linkProvider = async (providerId) => {
    const { authorizationUrl } = await startSocialLink(providerId);
    window.location.assign(authorizationUrl);
  };

  const unlinkProvider = async (providerId) => {
    await unlinkSocialProvider(providerId);
    await refreshSession?.();
  };

  const toggleRepo = (fullName) => {
    setMessage('');
    setSelectedRepos((current) => {
      if (current.includes(fullName)) {
        return current.filter((name) => name !== fullName);
      }
      if (current.length >= MAX_PINNED_REPOS) {
        setMessage(`대표 레포는 최대 ${MAX_PINNED_REPOS}개까지 선택할 수 있습니다.`);
        return current;
      }
      return [...current, fullName];
    });
  };

  return (
    <>
      <MainColumn>
        <div className="page-title"><h1>연동 관리</h1></div>
        <div className="integrations__content">
          {authLoading && <p className="small muted">로그인 상태를 확인하는 중…</p>}
          {!loggedIn && !authLoading && (
            <p className="small muted">로그인하면 GitHub·블로그 연동과 대표 레포를 관리할 수 있어요.</p>
          )}
          {message && (
            <p role="status" className="small" style={{ color: 'var(--accent)' }}>
              {message}
            </p>
          )}
          {data.error && <p role="alert" className="small muted">{data.error}</p>}

          {loggedIn && <PasswordSetupForm key={me.id} />}

          <section className="card integrations__card">
            <h2>로그인 계정</h2>
            <p className="small muted">
              프로필에서 연결하지 않고 다른 제공자로만 로그인하면 별도 계정이 됩니다.
              같은 회원으로 쓰려면 아래에서 추가 연결하세요. 이메일이 달라도 연결할 수 있습니다.
              다른 회원에 이미 연결된 소셜 계정은 추가할 수 없습니다. 해당 계정으로 로그인해 주세요.
            </p>
            <div className="integrations__repos" role="list">
              {(authProviders || []).map((provider) => {
                const linked = linkedSet.has(provider.id);
                const name = PROVIDER_NAMES[provider.id] || provider.id;
                return (
                  <div key={provider.id} className="row-wrap" role="listitem" style={{ marginBottom: 8 }}>
                    <span className="small">
                      <strong>{name}</strong>
                      {' · '}
                      {linked ? '연결됨' : provider.enabled ? '미연결' : '앱 키 미설정'}
                    </span>
                    {loggedIn && provider.enabled && !linked && (
                      <Button type="button" onClick={() => run(() => linkProvider(provider.id), '')}>
                        연결
                      </Button>
                    )}
                    {loggedIn && linked && (
                      <Button
                        type="button"
                        variant="outline"
                        disabled={!passwordLoginEnabled && (linkedProviders || []).length <= 1}
                        onClick={() => run(
                          () => unlinkProvider(provider.id),
                          `${name} 연결을 해제했습니다.`,
                        )}
                      >
                        해제
                      </Button>
                    )}
                  </div>
                );
              })}
            </div>
          </section>

          <section className="card integrations__card">
            <h2>GitHub</h2>
            {github ? (
              <>
                <p className="small muted">GitHub 로그인으로 연결됨. 기여 잔디는 프로필에서 확인할 수 있어요.</p>
                <p className="small">
                  <strong>{github.externalId || data.contributions?.login}</strong>
                  {typeof data.contributions?.totalContributions === 'number'
                    ? ` · 최근 1년 기여 ${data.contributions.totalContributions}회`
                    : ''}
                </p>
                <div className="row-wrap">
                  <Button
                    type="button"
                    onClick={() => run(() => data.syncGithub(), 'GitHub 연동을 동기화했습니다.')}
                  >
                    동기화
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => run(
                      () => data.disconnectIntegration('github'),
                      'GitHub 연동이 해제됐습니다. 다시 쓰려면 GitHub로 로그인하세요.',
                    )}
                  >
                    연결 해제
                  </Button>
                </div>
              </>
            ) : (
              <p className="small muted">
                {authLoading
                  ? '연동 정보를 불러오는 중…'
                  : loggedIn
                    ? 'GitHub로 다시 로그인하면 잔디·계정 연동이 저장됩니다.'
                    : '로그인 후 GitHub 연동 정보가 표시됩니다.'}
              </p>
            )}
          </section>

          <section className="card integrations__card">
            <h2>대표 레포</h2>
            <p className="small muted">
              버튼을 눌러 선택·해제합니다. 최대
              {' '}
              {MAX_PINNED_REPOS}
              개까지 고를 수 있습니다.
              {' '}
              (
              {selectedRepos.length}
              /
              {MAX_PINNED_REPOS}
              )
            </p>
            <div className="integrations__repos" role="group" aria-label="대표 레포 선택">
              {pickerRepos.length === 0 ? (
                <p className="small muted">
                  {authLoading
                    ? '공개 레포를 불러오는 중…'
                    : loggedIn
                      ? '불러온 공개 레포가 없습니다.'
                      : '로그인 후 공개 레포 목록을 불러옵니다.'}
                </p>
              ) : pickerRepos.map((repo) => {
                const on = selectedRepos.includes(repo.fullName);
                return (
                  <ToggleChip
                    key={repo.fullName}
                    tone="tech"
                    on={on}
                    onClick={() => toggleRepo(repo.fullName)}
                  >
                    {repo.fullName}
                  </ToggleChip>
                );
              })}
            </div>
            <Button
              disabled={!loggedIn}
              onClick={() => run(
                () => data.pinRepos(selectedRepos),
                selectedRepos.length
                  ? `대표 레포 ${selectedRepos.length}개를 저장했습니다.`
                  : '대표 레포 고정을 모두 해제했습니다.',
              )}
            >
              대표 레포 저장
            </Button>
          </section>

          <section className="card integrations__card">
            <h2>블로그 RSS</h2>
            <p className="small muted">
              {blog?.connected
                ? `연결됨 · ${blog.platform} · ${blog.externalId}`
                : '검증 코드를 소개나 글에 넣은 뒤 확인하면 연결됩니다.'}
            </p>
            <label className="integrations__field">
              플랫폼
              <select
                value={platform}
                onChange={(event) => {
                  setPlatform(event.target.value);
                  setBlogVerify(null);
                }}
              >
                <option value="VELOG">Velog</option>
                <option value="TISTORY">Tistory</option>
                <option value="ETC">기타 (RSS URL)</option>
              </select>
            </label>
            {platform === 'ETC' ? (
              <label className="integrations__field">
                RSS 주소 (HTTPS)
                <input
                  value={rssUrl}
                  onChange={(event) => {
                    setRssUrl(event.target.value);
                    setBlogVerify(null);
                  }}
                  placeholder="https://example.com/rss"
                />
              </label>
            ) : (
              <label className="integrations__field">
                블로그 아이디 또는 프로필 주소
                <input
                  value={blogId}
                  onChange={(event) => {
                    setBlogId(event.target.value);
                    setBlogVerify(null);
                  }}
                  placeholder={platform === 'VELOG' ? 'https://velog.io/@아이디' : 'https://아이디.tistory.com'}
                />
              </label>
            )}
            {blogVerify && (
              <div className="integrations__field">
                <p className="small muted">{blogVerify.hint || '아래 코드를 소개 또는 글에 넣고, 15분 안에 확인해 주세요.'}</p>
                <p className="small">
                  <strong>{blogVerify.code}</strong>
                </p>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => run(async () => {
                    await copyVerificationCode(blogVerify.code);
                  }, '검증 코드를 복사했습니다.')}
                >
                  코드 복사
                </Button>
              </div>
            )}
            <div className="row-wrap">
              <Button
                onClick={() => run(async () => {
                  const started = await data.startBlogVerify(
                    platform,
                    platform === 'ETC' ? null : blogId,
                    platform === 'ETC' ? rssUrl : null,
                  );
                  setBlogVerify(started);
                }, '검증 코드를 발급했습니다. 블로그에 넣은 뒤 확인해 주세요.')}
                disabled={!loggedIn || (platform === 'ETC' ? !rssUrl.trim() : !blogId.trim())}
              >
                검증 코드 받기
              </Button>
              <Button
                type="button"
                onClick={() => run(async () => {
                  await data.confirmBlogVerify();
                  setBlogVerify(null);
                }, '블로그 소유를 확인했고 연결했습니다.')}
                disabled={!loggedIn || !blogVerify}
              >
                확인
              </Button>
            </div>
            {blog?.connected && (
              <div className="row-wrap">
                <Button
                  type="button"
                  onClick={() => run(() => data.syncBlog(), '블로그 글을 동기화했습니다.')}
                >
                  동기화
                </Button>
                <Button
                  variant="outline"
                  onClick={() => run(() => data.disconnectIntegration('blog'), '블로그 연결이 해제됐습니다.')}
                >
                  연결 해제
                </Button>
              </div>
            )}
          </section>
        </div>
      </MainColumn>
      <RightAside>
        <section className="card">
          <h2>프로필 보기</h2>
          <Link href="/profile" className="text-link">내 프로필로 돌아가기</Link>
        </section>
      </RightAside>
    </>
  );
}
