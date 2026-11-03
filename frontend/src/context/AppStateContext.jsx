import { createContext, useContext, useMemo, useState } from 'react';
import { ME, POSTS, FEEDBACK_REQUESTS, COMMON_RESUMES, PUBLIC_RESUME_IDS, THREADS } from '../data/mock.js';

// 여러 화면이 함께 쓰는 상태. 실제 서비스에서는 서버 API + 캐시(React Query 등)로 대체한다.
const AppStateContext = createContext(null);

let seq = 1000;
const nextId = () => ++seq;

const feedbackCardId = (resumeId) => `resume-${resumeId}`;

export function AppStateProvider({ children }) {
  const [loggedIn, setLoggedIn] = useState(true);
  const [posts, setPosts] = useState(POSTS);
  const [liked, setLiked] = useState({});
  const [following, setFollowing] = useState({});
  const [applied, setApplied] = useState({});
  const [commonResumes, setCommonResumes] = useState(COMMON_RESUMES);
  const [publicResumeIds, setPublicResumeIds] = useState(PUBLIC_RESUME_IDS);
  const [feedbackRequests, setFeedbackRequests] = useState(FEEDBACK_REQUESTS);
  const [threads, setThreads] = useState(THREADS);

  const value = useMemo(() => {
    const removeFeedbackCard = (resumeId) =>
      setFeedbackRequests((list) => list.filter((r) => r.id !== feedbackCardId(resumeId)));

    return {
      me: ME,
      loggedIn,
      login: () => setLoggedIn(true),
      logout: () => setLoggedIn(false),

      // 피드
      posts,
      addPost: ({ type, body, tags = [], source = '', code = null }) =>
        setPosts((list) => [
          { id: nextId(), type, author: ME.name, handle: ME.handle, initial: ME.initial, av: ME.av, time: '방금', body, tags, source, code, replies: 0, reposts: 0, likes: 0 },
          ...list
        ]),
      liked,
      toggleLike: (id) => setLiked((m) => ({ ...m, [id]: !m[id] })),
      following,
      toggleFollow: (handle) => setFollowing((m) => ({ ...m, [handle]: !m[handle] })),

      // 공고 지원 (일반 공고: 고른 공통 이력서와 함께 보낸 정보를 기록)
      applied,
      applyJob: (jobId, submission) => setApplied((m) => ({ ...m, [jobId]: submission })),
      cancelApply: (jobId) => setApplied((m) => ({ ...m, [jobId]: null })),

      // 공통 이력서
      commonResumes,
      addCommonResume: () => {
        const id = nextId();
        setCommonResumes((list) => [
          ...list,
          { id, name: `새 공통 이력서 ${list.length + 1}`, field: '프론트엔드', sections: '아직 작성한 섹션이 없어요', updated: '방금 만듦', feedback: 0, bases: 0, skills: [], projects: [] }
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
        setPublicResumeIds((ids) => (ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id])),

      // 피드백 게시판
      feedbackRequests,
      isResumeRequested: (id) => feedbackRequests.some((r) => r.id === feedbackCardId(id)),
      toggleResumeRequest: (resume) =>
        setFeedbackRequests((list) =>
          list.some((r) => r.id === feedbackCardId(resume.id))
            ? list.filter((r) => r.id !== feedbackCardId(resume.id))
            : [
                {
                  id: feedbackCardId(resume.id), mine: true, author: ME.name, handle: ME.handle, initial: ME.initial, av: ME.av, time: '방금', field: resume.field,
                  resumeType: '공통 이력서', resumeTitle: resume.name, sections: resume.sections,
                  message: '이 이력서를 봐주세요.', focus: [], feedbacks: []
                },
                ...list
              ]
        ),
      addFeedback: (requestId, section, text) =>
        setFeedbackRequests((list) =>
          list.map((r) =>
            r.id === requestId
              ? { ...r, feedbacks: [...r.feedbacks, { id: `f${nextId()}`, author: ME.name, initial: ME.initial, av: ME.av, section, time: '방금', text }] }
              : r
          )
        ),
      adoptFeedback: (requestId, index) =>
        setFeedbackRequests((list) => list.map((r) => (r.id === requestId ? { ...r, adopted: index } : r))),

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
  }, [loggedIn, posts, liked, following, applied, commonResumes, publicResumeIds, feedbackRequests, threads]);

  return <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>;
}

export function useAppState() {
  return useContext(AppStateContext);
}
