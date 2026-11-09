'use client';

import { getAuthSession, logoutSession } from '../services/auth.js';
import { createContext, useContext, useMemo, useState, useEffect, useCallback } from 'react';
import { GUEST_ME, MAX_PUBLIC_RESUMES } from '../data/constants.js';

// 여러 화면이 함께 쓰는 상태. 실제 서비스에서는 서버 API + 캐시로 대체한다.
const AppStateContext = createContext(null);

let seq = 1000;
const nextId = () => ++seq;

const feedbackCardId = (resumeId) => `resume-${resumeId}`;

export function AppStateProvider({ children }) {
  const [session, setSession] = useState(null);
  const [authError, setAuthError] = useState('');
  useEffect(() => {
    const abort = new AbortController();
    getAuthSession({ signal: abort.signal }).then(setSession).catch(() => {
      if (!abort.signal.aborted) {
        setSession({ authenticated: false, user: null, providers: [], linkedProviders: [] });
        setAuthError('');
      }
    });
    return () => abort.abort();
  }, []);
  const refreshSession = useCallback(async () => {
    const current = await getAuthSession();
    setSession(current);
    return current;
  }, []);
  const loggedIn = session?.authenticated === true;
  const member = session?.user;
  const me = member
    ? {
        id: member.id,
        name: member.nickname || member.username,
        handle: member.username,
        // 회원 역할(USER, COMPANY, ADMIN). 좌측 메뉴를 역할별로 나눌 때 쓴다.
        role: member.role,
        initial: (member.nickname || member.username).slice(0, 1),
        av: 'av1',
        github: '',
      }
    : GUEST_ME;
  const [composeRequested, setComposeRequested] = useState(false);
  const [posts, setPosts] = useState([]);
  const [liked, setLiked] = useState({});
  const [following, setFollowing] = useState({});
  const [applied, setApplied] = useState({});
  const [commonResumes, setCommonResumes] = useState([]);
  const [publicResumeIds, setPublicResumeIds] = useState([]);
  const [feedbackRequests, setFeedbackRequests] = useState([]);
  const [threads, setThreads] = useState({});

  const value = useMemo(() => {
    const removeFeedbackCard = (resumeId) =>
      setFeedbackRequests((list) => list.filter((r) => r.id !== feedbackCardId(resumeId)));

    return {
      me,
      authLoading: session === null && !authError,
      authError,
      authProviders: session?.providers || [],
      linkedProviders: session?.linkedProviders || [],
      loggedIn,
      passwordLoginEnabled: session?.passwordLoginEnabled === true,
      refreshSession,
      logout: async () => {
        await logoutSession();
        setSession(await getAuthSession());
      },

      composeRequested,
      requestCompose: () => setComposeRequested(true),
      clearComposeRequest: () => setComposeRequested(false),

      posts,
      addPost: ({ type, body, tags = [], source = '', code = null }) =>
        setPosts((list) => [
          {
            id: nextId(),
            type,
            author: me.name,
            handle: me.handle,
            initial: me.initial,
            av: me.av,
            time: '방금',
            body,
            tags,
            source,
            code,
            replies: 0,
            reposts: 0,
            likes: 0,
          },
          ...list,
        ]),
      liked,
      setPostLiked: (key, value) => setLiked((previous) => ({ ...previous, [key]: value })),
      toggleLike: (id) => setLiked((m) => ({ ...m, [id]: !m[id] })),
      following,
      setFollowState: (handle, value) => setFollowing((m) => ({ ...m, [handle]: !!value })),
      toggleFollow: (handle) => setFollowing((m) => ({ ...m, [handle]: !m[handle] })),

      applied,
      applyJob: (jobId, submission) => setApplied((m) => ({ ...m, [jobId]: submission })),
      cancelApply: (jobId) => setApplied((m) => ({ ...m, [jobId]: null })),

      commonResumes,
      addCommonResume: () => {
        const id = nextId();
        setCommonResumes((list) => [
          ...list,
          {
            id,
            name: `새 공통 이력서 ${list.length + 1}`,
            field: '프론트엔드',
            sections: '아직 작성한 섹션이 없어요',
            updated: '방금 만듦',
            feedback: 0,
            bases: 0,
            skills: [],
            projects: [],
          },
        ]);
        return id;
      },
      renameCommonResume: (id, name) =>
        setCommonResumes((list) => list.map((r) => (r.id === id ? { ...r, name, updated: '방금 수정' } : r))),
      removeCommonResume: (id) => {
        setCommonResumes((list) => list.filter((r) => r.id !== id));
        setPublicResumeIds((ids) => ids.filter((x) => x !== id));
        removeFeedbackCard(id);
      },
      publicResumeIds,
      togglePublicResume: (id) =>
        setPublicResumeIds((ids) => {
          if (ids.includes(id)) return ids.filter((x) => x !== id);
          if (ids.length >= MAX_PUBLIC_RESUMES) return ids;
          return [...ids, id];
        }),

      feedbackRequests,
      isResumeRequested: (id) => feedbackRequests.some((r) => r.id === feedbackCardId(id)),
      toggleResumeRequest: (resume) =>
        setFeedbackRequests((list) =>
          list.some((r) => r.id === feedbackCardId(resume.id))
            ? list.filter((r) => r.id !== feedbackCardId(resume.id))
            : [
                {
                  id: feedbackCardId(resume.id),
                  mine: true,
                  author: me.name,
                  handle: me.handle,
                  initial: me.initial,
                  av: me.av,
                  time: '방금',
                  field: resume.field,
                  resumeType: '공통 이력서',
                  resumeTitle: resume.name,
                  sections: resume.sections,
                  message: '이 이력서를 봐주세요.',
                  focus: [],
                  feedbacks: [],
                },
                ...list,
              ]),
      addFeedback: (requestId, section, text) =>
        setFeedbackRequests((list) =>
          list.map((r) =>
            r.id === requestId
              ? {
                  ...r,
                  feedbacks: [
                    ...r.feedbacks,
                    {
                      id: `f${nextId()}`,
                      author: me.name,
                      initial: me.initial,
                      av: me.av,
                      section,
                      time: '방금',
                      text,
                    },
                  ],
                }
              : r)),
      adoptFeedback: (requestId, index) =>
        setFeedbackRequests((list) => list.map((r) => (r.id === requestId ? { ...r, adopted: index } : r))),

      threads,
      addAnswer: (threadId, body) =>
        setThreads((m) => {
          const current = m[threadId];
          if (!current) return m;
          return {
            ...m,
            [threadId]: {
              ...current,
              answers: [
                ...current.answers,
                {
                  id: nextId(),
                  author: me.name,
                  handle: me.handle,
                  initial: me.initial,
                  av: me.av,
                  time: '방금',
                  likes: 0,
                  body,
                },
              ],
            },
          };
        }),
    };
  }, [
    session, authError, loggedIn, refreshSession, me, composeRequested, posts, liked, following,
    applied, commonResumes, publicResumeIds, feedbackRequests, threads,
  ]);

  return <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>;
}

export function useAppState() {
  return useContext(AppStateContext);
}
