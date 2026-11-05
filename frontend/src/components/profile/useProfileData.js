'use client';

import { useEffect, useState } from 'react';
import { profileApi } from '../../services/profileApi.js';

const EMPTY_PAGES = { posts: 0, tech: 0, feedback: 0 };
const EMPTY_MORE = { posts: false, tech: false, feedback: false };

/**
 * @param {object} [options]
 * @param {boolean} [options.ready] 세션 확인이 끝난 뒤에만 API를 호출한다
 */
export default function useProfileData(
  username,
  isOwner,
  initialProfile,
  activeTab = 'posts',
  viewer,
  options = {},
) {
  const ready = options.ready !== false;

  const [profile, setProfile] = useState(initialProfile || null);
  const [loading, setLoading] = useState(Boolean(ready && !initialProfile));
  const [ownedResumes, setOwnedResumes] = useState(null);
  const [progress, setProgress] = useState(null);
  const [posts, setPosts] = useState(null);
  const [techPosts, setTechPosts] = useState(null);
  const [feedbacks, setFeedbacks] = useState(null);
  const [pages, setPages] = useState(EMPTY_PAGES);
  const [hasMore, setHasMore] = useState(EMPTY_MORE);
  const [integrations, setIntegrations] = useState(null);
  const [repos, setRepos] = useState(null);
  const [candidateRepos, setCandidateRepos] = useState(null);
  const [blogPosts, setBlogPosts] = useState(null);
  const [contributions, setContributions] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    if (!ready || !username) {
      setLoading(Boolean(ready && !username));
      return () => { active = false; };
    }

    setPosts(null);
    setTechPosts(null);
    setFeedbacks(null);
    setPages(EMPTY_PAGES);
    setHasMore(EMPTY_MORE);
    setRepos(null);
    setCandidateRepos(null);
    setBlogPosts(null);
    setContributions(null);
    setProgress(null);
    setIntegrations(null);
    if (!initialProfile) setProfile(null);

    const load = async () => {
      if (active) setLoading(true);
      try {
        const data = isOwner
          ? await profileApi.getMine(username)
          : await profileApi.getPublic(username, viewer);
        if (!active) return;
        setProfile(data.profile || data);
        setOwnedResumes(data.resumes || null);
        setError('');
      } catch {
        if (active) {
          setError(initialProfile
            ? '최신 프로필을 불러오지 못했습니다.'
            : '프로필을 불러오지 못했습니다. 다시 시도해 주세요.');
        }
      } finally {
        if (active) setLoading(false);
      }
      if (isOwner) {
        try {
          const nextProgress = await profileApi.getReviewerProgress(username);
          if (active) setProgress(nextProgress);
        } catch { /* ignore */ }
        try {
          const linked = await profileApi.getIntegrations(username);
          if (active) setIntegrations(linked);
        } catch { /* ignore */ }
      }
    };
    load();
    return () => { active = false; };
  }, [username, isOwner, initialProfile, viewer, ready]);

  useEffect(() => {
    if (!ready || !username) return undefined;
    let active = true;
    profileApi.getContributions(username).then((data) => {
      if (!active) return;
      setContributions({
        login: data.login,
        totalContributions: data.totalContributions,
        weeks: data.weeks,
      });
    }).catch(() => {});
    return () => { active = false; };
  }, [username, ready]);

  useEffect(() => {
    if (!ready || !username) return undefined;
    let active = true;
    profileApi.getPosts(username).then((data) => {
      if (active) {
        setPosts(data.items);
        setHasMore((state) => ({ ...state, posts: data.page + 1 < data.totalPages }));
      }
    }).catch(() => { if (active) setPosts([]); });
    profileApi.getFeedbacks(username).then((data) => {
      if (active) {
        setFeedbacks(data.items);
        setHasMore((state) => ({ ...state, feedback: data.page + 1 < data.totalPages }));
      }
    }).catch(() => { if (active) setFeedbacks([]); });
    profileApi.getRepos(username, true).then((items) => {
      if (active) setRepos(items);
    }).catch(() => { if (active) setRepos([]); });
    if (isOwner) {
      profileApi.getRepos(username).then((items) => {
        if (active) setCandidateRepos(items);
      }).catch(() => { if (active) setCandidateRepos([]); });
    } else {
      setCandidateRepos(null);
    }
    profileApi.getBlogPosts(username).then((items) => {
      if (active) setBlogPosts(items);
    }).catch(() => { if (active) setBlogPosts([]); });
    return () => { active = false; };
  }, [username, isOwner, ready]);

  useEffect(() => {
    if (!ready || !username || activeTab !== 'tech') return undefined;
    let active = true;
    profileApi.getPosts(username, 'tech').then((data) => {
      if (active) {
        setTechPosts(data.items);
        setHasMore((state) => ({ ...state, tech: data.page + 1 < data.totalPages }));
      }
    }).catch(() => { if (active) setTechPosts([]); });
    return () => { active = false; };
  }, [username, activeTab, ready]);

  const updateProfile = async (body) => {
    const data = await profileApi.updateMine(username, body);
    setProfile(data.profile);
    setOwnedResumes(data.resumes);
  };

  const pinResume = async (id) => {
    setProfile(await profileApi.pinResume(username, id));
  };

  const displayBadge = async (id) => {
    setProfile(await profileApi.displayBadge(username, id));
  };

  const setVisibility = async (id, isPublic) => {
    const data = await profileApi.setVisibility(username, id, isPublic);
    setProfile(data.profile);
    setOwnedResumes(data.resumes);
  };

  const connectBlog = async (platform, blogId) => {
    const linked = await profileApi.connectBlog(username, platform, blogId);
    setIntegrations((items) => [...(items || []).filter((item) => item.provider !== 'BLOG'), linked]);
  };

  const disconnectIntegration = async (provider) => {
    const linked = await profileApi.disconnectIntegration(username, provider);
    setIntegrations((items) => [...(items || []).filter((item) => item.provider !== linked.provider), linked]);
  };

  const pinRepos = async (names) => {
    const pinned = await profileApi.pinRepos(username, names);
    setRepos(pinned);
    try {
      setCandidateRepos(await profileApi.getRepos(username));
    } catch {
      /* keep previous candidates */
    }
    return pinned;
  };

  const loadMore = async (tab) => {
    const key = tab === 'feedback' ? 'feedback' : tab === 'tech' ? 'tech' : 'posts';
    const nextPage = pages[key] + 1;
    const response = key === 'feedback'
      ? await profileApi.getFeedbacks(username, nextPage)
      : await profileApi.getPosts(username, key === 'tech' ? 'tech' : null, nextPage);
    if (key === 'feedback') setFeedbacks((items) => [...(items || []), ...response.items]);
    else if (key === 'tech') setTechPosts((items) => [...(items || []), ...response.items]);
    else setPosts((items) => [...(items || []), ...response.items]);
    setPages((state) => ({ ...state, [key]: nextPage }));
    setHasMore((state) => ({ ...state, [key]: nextPage + 1 < response.totalPages }));
  };

  const patchProfile = (partial) => {
    setProfile((prev) => (prev ? { ...prev, ...partial } : prev));
  };

  return {
    profile, loading, ownedResumes, progress, posts, techPosts, feedbacks, integrations,
    repos, candidateRepos, blogPosts, contributions, hasMore, error, updateProfile, pinResume,
    displayBadge, setVisibility, connectBlog, disconnectIntegration, pinRepos, loadMore, setError,
    patchProfile,
  };
}
