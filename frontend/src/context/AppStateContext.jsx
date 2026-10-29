import { createContext, useContext, useMemo, useState } from 'react';
import { ME, POSTS, FEEDBACK_REQUESTS, MY_FEEDBACK_REQUEST, THREADS } from '../data/mock.js';

// 여러 화면이 함께 쓰는 상태. 실제 서비스에서는 서버 API + 캐시(React Query 등)로 대체한다.
const AppStateContext = createContext(null);

let seq = 1000;
const nextId = () => ++seq;

export function AppStateProvider({ children }) {
  const [loggedIn, setLoggedIn] = useState(true);
  const [posts, setPosts] = useState(POSTS);
  const [liked, setLiked] = useState({});
  const [following, setFollowing] = useState({});
  const [applied, setApplied] = useState({});
  const [feedbackRequests, setFeedbackRequests] = useState(FEEDBACK_REQUESTS);
  const [threads, setThreads] = useState(THREADS);

  const value = useMemo(() => {
    const myRequested = feedbackRequests.some((r) => r.mine);
    return {
      me: ME,
      loggedIn,
      login: () => setLoggedIn(true),
      logout: () => setLoggedIn(false),

      // 피드
      posts,
      addPost: ({ type, body, tags = [], source = '' }) =>
        setPosts((list) => [
          { id: nextId(), type, author: ME.name, handle: ME.handle, initial: ME.initial, av: ME.av, time: '방금', body, tags, source, replies: 0, reposts: 0, likes: 0 },
          ...list
        ]),
      liked,
      toggleLike: (id) => setLiked((m) => ({ ...m, [id]: !m[id] })),
      following,
      toggleFollow: (handle) => setFollowing((m) => ({ ...m, [handle]: !m[handle] })),

      // 공고
      applied,
      toggleApply: (jobId) => setApplied((m) => ({ ...m, [jobId]: !m[jobId] })),

      // 피드백 게시판
      feedbackRequests,
      myRequested,
      toggleMyRequest: () =>
        setFeedbackRequests((list) => (list.some((r) => r.mine) ? list.filter((r) => !r.mine) : [MY_FEEDBACK_REQUEST, ...list])),
      addFeedback: (requestId, section, text) =>
        setFeedbackRequests((list) =>
          list.map((r) =>
            r.id === requestId
              ? { ...r, feedbacks: [...r.feedbacks, { id: `f${nextId()}`, author: ME.name, initial: ME.initial, av: ME.av, section, time: '방금', text }] }
              : r
          )
        ),

      // 질문 스레드
      threads,
      addAnswer: (threadId, body) =>
        setThreads((m) => ({
          ...m,
          [threadId]: {
            ...m[threadId],
            answers: [...m[threadId].answers, { id: nextId(), author: ME.name, handle: ME.handle, initial: ME.initial, av: ME.av, time: '방금', likes: 0, body }]
          }
        }))
    };
  }, [loggedIn, posts, liked, following, applied, feedbackRequests, threads]);

  return <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>;
}

export function useAppState() {
  return useContext(AppStateContext);
}
