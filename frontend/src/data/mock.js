// =========================================================
// 예시(목업) 데이터. 실제 API가 붙으면 이 파일 대신 api/ 레이어에서 받아온다.
// av: 아바타 색 토큰 이름 (styles/tokens.css 의 --av1 ~ --av6, --co1 ~ --co4)
// =========================================================

export const ME = { name: '이서연', handle: 'seoyeon_dev', initial: '서', av: 'av1', github: 'seoyeon-dev' };

// 게시글 type: general | tech | question | blog | repo | job
export const POSTS = [
  {
    id: 8, type: 'tech', author: '한민서', handle: 'minseo_be', initial: '민', av: 'av6', time: '40분',
    body: 'Spring Boot 3로 올릴 때 가장 많이 막히는 부분 정리\n1. javax → jakarta 패키지명 변경\n2. Java 17 이상 필요\n3. Spring Security 6에서 WebSecurityConfigurerAdapter 제거 → SecurityFilterChain 빈으로 설정',
    tags: ['Spring', 'Java'], source: 'Spring Boot 3.0 마이그레이션 가이드', replies: 8, reposts: 31, likes: 120
  },
  {
    id: 1, type: 'question', threadId: 1, author: '김도윤', handle: 'doyoon_cs', initial: '도', av: 'av2', time: '1시간',
    body: '신입 지원할 때 포트폴리오 링크는 이력서 맨 위에 두는 게 좋을까요, 프로젝트 섹션에 두는 게 좋을까요?',
    replies: 4, reposts: 2, likes: 18
  },
  {
    id: 6, type: 'blog', author: '정우진', handle: 'woojin_fe', initial: '우', av: 'av4', time: '2시간',
    body: '무한 스크롤에서 캐시가 꼬였던 문제를 해결한 과정을 정리했어요.',
    blog: { source: '벨로그', title: 'React Query 무한 스크롤에서 캐시가 꼬일 때', desc: 'queryKey 설계부터 낙관적 업데이트까지, 실제 팀 프로젝트에서 겪은 문제 정리' },
    evidence: '이력서 프로젝트 「스터디 매칭 웹앱」의 근거로 연결됨', replies: 6, reposts: 14, likes: 52
  },
  {
    id: 7, type: 'repo', author: '박지훈', handle: 'jihoon_be', initial: '지', av: 'av5', time: '3시간',
    body: '3계층 구조로 만든 게시판 예제 레포 공개했어요. 구조 리뷰 환영합니다.',
    repo: { name: 'jihoon/spring-layered-board', desc: 'Controller · Service · Repository 3계층 게시판 예제', lang: 'Java', langColor: '#B07219', stars: 24, updated: '어제 업데이트' },
    evidence: '이력서 프로젝트 「사내 게시판 API」의 근거로 연결됨', replies: 3, reposts: 6, likes: 30
  },
  {
    id: 9, type: 'tech', author: '정우진', handle: 'woojin_fe', initial: '우', av: 'av4', time: '5시간',
    body: 'React 19에서 새로 생긴 훅 한눈에 보기\n· use — Promise나 Context를 렌더링 중에 바로 읽기\n· useActionState — 폼 액션의 결과와 대기 상태 관리\n· useOptimistic — 서버 응답 전에 화면 먼저 바꾸기',
    tags: ['React', 'TypeScript'], source: 'React 19 릴리스 노트', replies: 11, reposts: 40, likes: 158
  },
  {
    id: 3, type: 'general', author: '최하은', handle: 'haeun_ux', initial: '하', av: 'av3', time: '3시간',
    body: '오늘 첫 기술 면접 다녀왔어요. 질문 절반이 이력서에 쓴 프로젝트에서 나왔네요.\n이력서에 쓴 건 전부 설명할 수 있게 준비하세요!',
    replies: 7, reposts: 9, likes: 63
  },
  {
    id: 4, type: 'job', jobId: 3, company: true, author: '노바랩스', handle: 'novalabs', initial: 'N', av: 'co1', time: '5시간',
    body: '백엔드 개발자를 채용합니다. 공통 이력서로 바로 지원할 수 있어요.', replies: 2, reposts: 5, likes: 22
  },
  {
    id: 5, type: 'job', jobId: 1, company: true, author: '블루웨일 스튜디오', handle: 'bluewhale', initial: 'B', av: 'co2', time: '1일',
    body: '프론트엔드 인턴을 찾고 있어요. 저희가 궁금한 질문 두 가지에 답해주세요.', replies: 5, reposts: 11, likes: 37
  }
];

// 공유자 배지: 실제로는 "태그별 기술 글 N개 이상" 규칙으로 서버에서 계산
export const SHARER_BADGES = { woojin_fe: 'React 공유자', minseo_be: 'Spring 공유자', jihoon_be: 'Java 공유자' };

export const TECH_TAGS = ['React', 'TypeScript', 'Spring', 'Java', 'Docker', 'AI'];

// 공고 type: general(공통 이력서 제출) | special(요구 문항 + 특화 이력서)
export const JOBS = [
  {
    id: 1, type: 'special', company: '블루웨일 스튜디오', handle: 'bluewhale', initial: 'B', av: 'co2',
    title: '프론트엔드 인턴 (React · TypeScript)', meta: '서울 마포 · 인턴 3개월', dday: 'D-5',
    intro: '사용자가 직접 쓰는 웹 서비스의 화면을 함께 만들 인턴을 찾습니다. 디자이너, 백엔드 개발자와 매일 같이 일하며 실제 배포까지 경험하게 됩니다.',
    requirements: ['React로 만든 프로젝트 경험', 'Git으로 협업해 본 경험', 'HTML · CSS 기본기'],
    preferred: ['TypeScript 사용 경험', '디자인 시스템 이해', 'GitHub 활동 기록'],
    questions: [
      { text: '가장 어려웠던 UI 문제와 해결 과정을 알려주세요.', limit: 500 },
      { text: '블루웨일 서비스 중 하나를 골라 개선점을 제안해 주세요.', limit: 800 }
    ]
  },
  { id: 2, type: 'general', company: '코어브릿지', handle: 'corebridge', initial: 'C', av: 'co3', title: '데이터 엔지니어', meta: '서울 강남 · 정규직 · 경력 1~3년 · Python · Airflow', dday: 'D-7' },
  { id: 3, type: 'general', company: '노바랩스', handle: 'novalabs', initial: 'N', av: 'co1', title: '백엔드 개발자 (신입 · 경력 1~3년)', meta: '서울 성수 · 정규직 · Java · Spring Boot · MySQL', dday: 'D-12' },
  {
    id: 4, type: 'special', company: '모아핀', handle: 'moafin', initial: 'M', av: 'co4', title: '주니어 프론트엔드', meta: '판교 · 정규직', dday: 'D-15',
    intro: '금융 데이터를 쉽게 보여 주는 화면을 함께 만들 주니어를 찾습니다.',
    requirements: ['React 실무 또는 프로젝트 경험'], preferred: ['차트 라이브러리 경험'],
    questions: [{ text: '데이터를 시각화하며 가장 신경 쓴 점을 알려주세요.', limit: 600 }]
  },
  { id: 5, type: 'general', company: '블루웨일 스튜디오', handle: 'bluewhale', initial: 'B', av: 'co2', title: 'UI 디자이너 인턴', meta: '서울 마포 · 인턴 3개월 · Figma', dday: 'D-20' }
];

export const COMMON_RESUME = {
  title: '프론트엔드 개발자 · 이서연',
  sections: '학력 · 기술 스택 · 프로젝트 3개 · 수상 1개',
  basic: { name: '이서연', school: '[학교명] 컴퓨터공학과', github: 'github.com/[아이디]' },
  skills: ['React', 'TypeScript', 'Next.js', 'Tailwind', 'Git'],
  projects: [
    { id: 'p1', title: '스터디 매칭 웹앱', desc: 'React · 팀 4명 · 검색 필터와 무한 스크롤 구현', evidence: [{ type: 'repo', label: 'study-match-web · TypeScript' }, { type: 'blog', label: '벨로그 · 무한 스크롤 구현기' }] },
    { id: 'p2', title: '디자인 시스템 컴포넌트 라이브러리', desc: 'TypeScript · 개인 · 버튼 · 모달 등 12개 컴포넌트', evidence: [{ type: 'repo', label: 'button-kit · TypeScript' }] },
    { id: 'p3', title: '알고리즘 풀이 블로그', desc: 'Next.js · 개인', evidence: [] }
  ]
};

export const SPECIAL_RESUMES = [
  { id: 1, jobId: 1, initial: 'B', av: 'co2', company: '블루웨일 스튜디오', role: '프론트엔드 인턴', base: '공통 이력서 기반', questions: 2, updated: '방금 전', status: 'writing' },
  { id: 2, jobId: null, initial: 'C', av: 'co3', company: '코어브릿지', role: '웹 개발 인턴', base: '처음부터 작성', questions: 3, updated: '2일 전', status: 'submitted' },
  { id: 3, jobId: 4, initial: 'M', av: 'co4', company: '모아핀', role: '주니어 프론트엔드', base: '공통 이력서 기반', questions: 1, updated: '1주 전', status: 'submitted' }
];

// 피드백 게시판에 올라온 이력서 (내 요청은 AppStateContext 에서 추가된다)
export const FEEDBACK_REQUESTS = [
  {
    id: 2, author: '김도윤', handle: 'doyoon_cs', initial: '도', av: 'av2', time: '4시간', field: '백엔드',
    resumeType: '공통 이력서', resumeTitle: '백엔드 개발자 · 김도윤', sections: '학력 · 기술 스택 · 프로젝트 2개',
    message: '신입이라 경력란이 비어 있어요. 프로젝트로 채우는 게 맞을까요?', focus: ['프로젝트', '자기소개'],
    feedbacks: [
      { id: 'f1', author: '노바랩스', initial: 'N', av: 'co1', company: true, section: '프로젝트', time: '1시간', text: '경력란은 비워 두고, 프로젝트에서 맡은 API와 처리한 데이터 규모를 숫자로 적어 주시면 충분히 판단할 수 있어요.' }
    ]
  },
  {
    id: 3, author: '정우진', handle: 'woojin_fe', initial: '우', av: 'av4', time: '6시간', field: '프론트엔드',
    resumeType: '특화 이력서 · 회사명 비공개', resumeTitle: '프론트엔드 인턴 지원서', sections: '기술 스택 · 프로젝트 2개 · 요구 문항 2개',
    message: '요구 문항 답변이 회사가 원하는 방향인지 봐주세요.', focus: ['자기소개'], feedbacks: []
  },
  {
    id: 4, author: '한민서', handle: 'minseo_be', initial: '민', av: 'av6', time: '1일', field: '백엔드',
    resumeType: '공통 이력서', resumeTitle: '백엔드 개발자 · 한민서', sections: '학력 · 기술 스택 · 프로젝트 4개',
    message: 'Spring 프로젝트 두 개 중 어떤 걸 대표로 둘지 고민이에요.', focus: ['프로젝트'],
    feedbacks: [
      { id: 'f2', author: '박지훈', initial: '지', av: 'av5', section: '프로젝트', time: '20시간', text: '테스트 코드와 배포까지 해 본 쪽을 대표로 두세요. 면접에서 이야기할 거리가 훨씬 많아요.' }
    ]
  }
];

// 내가 피드백을 요청하면 게시판에 올라갈 카드 (요청 전부터 받은 피드백 2개 포함)
export const MY_FEEDBACK_REQUEST = {
  id: 1, mine: true, author: ME.name, handle: ME.handle, initial: ME.initial, av: ME.av, time: '방금', field: '프론트엔드',
  resumeType: '공통 이력서', resumeTitle: COMMON_RESUME.title, sections: COMMON_RESUME.sections,
  message: '프론트엔드 신입 이력서예요. 프로젝트 설명이 너무 길지는 않은지, 기술 스택 배치가 괜찮은지 봐주세요.', focus: ['프로젝트', '기술 스택'],
  feedbacks: [
    { id: 'f3', author: '박지훈', initial: '지', av: 'av5', section: '프로젝트', time: '30분', text: '프로젝트마다 "문제 → 해결 → 결과" 순서로 3줄씩만 정리해도 훨씬 잘 읽혀요.' },
    { id: 'f4', author: '최하은', initial: '하', av: 'av3', section: '기술 스택', time: '10분', text: '숙련도 높은 기술을 앞에 두고, 써 본 정도인 건 "경험 있음"으로 따로 묶는 걸 추천해요.' }
  ]
};

export const THREADS = {
  1: {
    id: 1, author: '김도윤', handle: 'doyoon_cs', initial: '도', av: 'av2', time: '오후 2:03 · 2026년 10월 29일',
    body: '신입 지원할 때 포트폴리오 링크는 이력서 맨 위에 두는 게 좋을까요, 프로젝트 섹션에 두는 게 좋을까요?',
    tags: ['신입', '포트폴리오'], reposts: 2, likes: 18,
    answers: [
      { id: 1, author: '박지훈', handle: 'jihoon_be', initial: '지', av: 'av5', time: '50분', likes: 14, accepted: true, body: '맨 위 연락처 줄에 한 번, 프로젝트마다 한 번씩 두는 걸 추천해요.\n인사 담당자는 위에서, 실무자는 프로젝트에서 링크를 찾더라고요.' },
      { id: 2, author: '최하은', handle: 'haeun_ux', initial: '하', av: 'av3', time: '40분', likes: 9, body: '링크만 두지 말고 무엇을 볼 수 있는지 한 줄 설명을 붙이면 클릭률이 확실히 달라요.' },
      { id: 3, author: '노바랩스', handle: 'novalabs', initial: 'N', av: 'co1', company: true, time: '25분', likes: 21, body: '채용 담당자 입장에서는 상단에 있는 편이 편합니다. 배포 링크가 있으면 같이 넣어 주세요.' },
      { id: 4, author: '정우진', handle: 'woojin_fe', initial: '우', av: 'av4', time: '10분', likes: 3, body: '저는 QR 코드도 같이 넣었는데 종이로 받는 면접관분들이 좋아하셨어요.' }
    ]
  }
};

export const REPOS = [
  { name: 'study-match-web', lang: 'TypeScript' },
  { name: 'button-kit', lang: 'TypeScript' },
  { name: 'algo-blog', lang: 'JavaScript' },
  { name: 'dotfiles', lang: 'Shell' }
];

// 블로그 플랫폼별 RSS 주소 규칙 (백엔드 RSS 수집기와 같은 규칙)
export const RSS_RULES = {
  velog: { label: '벨로그', input: '블로그 아이디', url: (id) => `https://v2.velog.io/rss/@${id}` },
  tistory: { label: '티스토리', input: '블로그 아이디', url: (id) => `https://${id}.tistory.com/rss` },
  etc: { label: '기타 블로그', input: 'RSS 주소', url: (v) => v }
};
