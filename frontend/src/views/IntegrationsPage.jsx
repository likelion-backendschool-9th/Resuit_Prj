'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Button from '../components/common/Button.jsx';
import { ToggleChip } from '../components/common/Chip.jsx';
import useProfileData from '../components/profile/useProfileData.js';
import './IntegrationsPage.css';

const MAX_PINNED_REPOS = 3;

export default function IntegrationsPage() {
  const { me, loggedIn, authLoading } = useAppState();
  const data = useProfileData(me.handle, true, null, 'posts', me.handle, {
    ready: !authLoading && loggedIn,
  });
  const github = data.integrations?.find((item) => item.provider === 'GITHUB' && item.connected);
  const [platform, setPlatform] = useState('VELOG');
  const [blogId, setBlogId] = useState('');
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

  useEffect(() => {
    if (!data.repos) return;
    setSelectedRepos(
      data.repos.map((repo) => repo.fullName).filter(Boolean).slice(0, MAX_PINNED_REPOS),
    );
  }, [selectedKey, data.repos]);

  const run = async (action, success) => {
    setMessage('');
    try {
      await action();
      setMessage(success);
    } catch {
      setMessage('요청을 처리하지 못했습니다. 연결 상태를 확인해 주세요.');
    }
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
          {message && <p role="status" className="small muted">{message}</p>}
          {data.error && <p role="alert" className="small muted">{data.error}</p>}

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
              {blog?.connected ? `연결됨 · ${blog.platform} · ${blog.externalId}` : '연결되지 않았습니다.'}
            </p>
            <label className="integrations__field">
              플랫폼
              <select value={platform} onChange={(event) => setPlatform(event.target.value)}>
                <option value="VELOG">Velog</option>
                <option value="TISTORY">Tistory</option>
              </select>
            </label>
            <label className="integrations__field">
              블로그 아이디
              <input value={blogId} onChange={(event) => setBlogId(event.target.value)} />
            </label>
            <div className="row-wrap">
              <Button
                onClick={() => run(() => data.connectBlog(platform, blogId), '블로그가 연결됐습니다.')}
                disabled={!loggedIn || !blogId.trim()}
              >
                연결
              </Button>
              {blog?.connected && (
                <Button
                  variant="outline"
                  onClick={() => run(() => data.disconnectIntegration('blog'), '블로그 연결이 해제됐습니다.')}
                >
                  연결 해제
                </Button>
              )}
            </div>
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
