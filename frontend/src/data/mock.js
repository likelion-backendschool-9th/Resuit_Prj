// =========================================================
// 예시(목업) 데이터. 실제 API가 붙으면 이 파일 대신 api/ 레이어에서 받아온다.
// av: 아바타 색 토큰 이름 (styles/tokens.css 의 --av1 ~ --av10, --co1 ~ --co4)
// =========================================================
import dockerImageSize from '../assets/feed/docker-image-size.jpg';
import feedbackErd from '../assets/feed/feedback-erd.jpg';
import githubGrass from '../assets/feed/github-grass.jpg';
import readmeTemplate from '../assets/feed/readme-template.jpg';
import feedDemo from '../assets/feed/feed-demo.mp4';
import feedDemoPoster from '../assets/feed/feed-demo-poster.jpg';
import darkModeDemo from '../assets/feed/dark-mode-demo.mp4';
import darkModeDemoPoster from '../assets/feed/dark-mode-demo-poster.jpg';

export const ME = { name: '이서연', handle: 'seoyeon_dev', initial: '서', av: 'av1', github: 'seoyeon-dev' };

// 게시글 type: general | tech | question | blog | repo | job
export const POSTS = [
  {
    id: 10, type: 'tech', author: '한민서', handle: 'minseo_be', initial: '민', av: 'av6', time: '20분',
    body: 'Docker 이미지 1.24GB → 182MB로 줄인 방법\n빌드 스테이지와 실행 스테이지를 나누고, 실행은 JRE 슬림 이미지만 쓰면 돼요. 배포 시간도 3분 40초에서 52초로 줄었어요.',
    image: dockerImageSize, imageAlt: 'Docker 이미지 크기 비교 그래프. 1.24GB에서 182MB로 85% 감소',
    tags: ['Docker', 'Spring'], source: 'Docker 공식 문서 · Multi-stage builds', replies: 14, reposts: 52, likes: 211
  },
  {
    id: 8, type: 'tech', author: '한민서', handle: 'minseo_be', initial: '민', av: 'av6', time: '40분',
    body: 'Spring Boot 3로 올릴 때 가장 많이 막히는 부분 정리\n1. javax → jakarta 패키지명 변경\n2. Java 17 이상 필요\n3. Spring Security 6에서 WebSecurityConfigurerAdapter 제거 → SecurityFilterChain 빈으로 설정',
    tags: ['Spring', 'Java'], source: 'Spring Boot 3.0 마이그레이션 가이드', replies: 8, reposts: 31, likes: 120
  },
  {
    id: 19, type: 'question', threadId: 1, author: '김도윤', handle: 'doyoon_cs', initial: '도', av: 'av2', time: '30분',
    body: 'JPA로 피드 20개 불러오는데 쿼리가 21번 나가요. 이게 N+1 맞죠?\nfetch join이랑 @EntityGraph 중에 뭘 쓰는 게 나을까요?',
    code: {
      file: 'PostService.java', lang: 'Java',
      text: '@Transactional(readOnly = true)\npublic List<PostResponse> getFeed(int page, int size) {\n    List<Post> posts = postRepository.findAll(PageRequest.of(page, size)).getContent();\n\n    // 게시글마다 작성자를 따로 조회 → 쿼리 1 + 20번\n    return posts.stream()\n        .map(post -> new PostResponse(\n            post.getId(),\n            post.getContent(),\n            post.getUser().getNickname(),   // 여기서 추가 쿼리\n            post.getLikes().size()          // 여기서도 추가 쿼리\n        ))\n        .toList();\n}\n\n// 실행 로그\n// select * from posts limit 20\n// select * from users where id = 1\n// select * from users where id = 2\n// ... (19번 더)'
    },
    replies: 7, reposts: 4, likes: 33
  },
  {
    id: 11, type: 'general', author: '최하은', handle: 'haeun_ux', initial: '하', av: 'av3', time: '1시간',
    body: '트위터 클론 토이 프로젝트 피드 화면 완성했어요.\n상단 탭 고정, 좋아요, 기술 태그 필터까지 넣었는데 UX 피드백 부탁드려요.',
    video: feedDemo, poster: feedDemoPoster, duration: '0:09',
    imageAlt: '피드를 스크롤하고 좋아요를 누른 뒤 기술 정보 탭에서 React 태그로 거르는 화면 녹화',
    evidence: '이력서 프로젝트 「SNS 클론」의 근거로 연결됨', replies: 9, reposts: 7, likes: 88
  },
  {
    id: 1, type: 'question', threadId: 1, author: '김도윤', handle: 'doyoon_cs', initial: '도', av: 'av2', time: '1시간',
    body: '신입 지원할 때 포트폴리오 링크는 이력서 맨 위에 두는 게 좋을까요, 프로젝트 섹션에 두는 게 좋을까요?',
    replies: 4, reposts: 2, likes: 18
  },
  {
    id: 12, type: 'question', threadId: 1, author: '이준호', handle: 'junho_be', initial: '준', av: 'av7', time: '1시간',
    body: '이력서 피드백 서비스 ERD인데요, 피드백 요청이 공통 이력서와 특화 이력서를 둘 다 참조해요.\nCHECK 제약으로 둘 중 하나만 들어가게 했는데 이 구조 괜찮을까요?',
    image: feedbackErd, imageAlt: '공통 이력서, 특화 이력서, 피드백 요청, 피드백 댓글 테이블이 연결된 ERD',
    replies: 6, reposts: 3, likes: 27
  },
  {
    id: 6, type: 'blog', author: '정우진', handle: 'woojin_fe', initial: '우', av: 'av4', time: '2시간',
    body: '무한 스크롤에서 캐시가 꼬였던 문제를 해결한 과정을 정리했어요.',
    blog: { source: '벨로그', title: 'React Query 무한 스크롤에서 캐시가 꼬일 때', desc: 'queryKey 설계부터 낙관적 업데이트까지, 실제 팀 프로젝트에서 겪은 문제 정리' },
    evidence: '이력서 프로젝트 「스터디 매칭 웹앱」의 근거로 연결됨', replies: 6, reposts: 14, likes: 52
  },
  {
    id: 15, type: 'general', author: '박지훈', handle: 'jihoon_be', initial: '지', av: 'av5', time: '2시간',
    body: '어제 받은 코드 리뷰 반영했어요. 컨트롤러에 있던 검증·저장 로직을 서비스로 옮겼더니 컨트롤러가 20줄로 줄었어요.\n3계층 나누는 게 왜 중요한지 이제 좀 알겠네요.',
    code: {
      file: 'FeedbackService.java', lang: 'Java',
      text: '@Service\n@RequiredArgsConstructor\npublic class FeedbackService {\n\n    private final FeedbackRequestRepository requestRepository;\n    private final FeedbackCommentRepository commentRepository;\n\n    // 컨트롤러에 있던 검증 · 저장 로직을 서비스로 이동\n    @Transactional\n    public Long addComment(Long requestId, Long userId, CommentRequest dto) {\n        FeedbackRequest request = requestRepository.findById(requestId)\n            .orElseThrow(() -> new NotFoundException("FEEDBACK_NOT_FOUND"));\n\n        if (request.isDeleted()) {\n            throw new BadRequestException("FEEDBACK_CLOSED");\n        }\n\n        FeedbackComment comment = dto.toEntity(request, userId);\n        return commentRepository.save(comment).getId();\n    }\n\n    @Transactional(readOnly = true)\n    public List<CommentResponse> getComments(Long requestId) {\n        return commentRepository.findAllByRequestIdOrderByCreatedAt(requestId)\n            .stream()\n            .map(CommentResponse::from)\n            .toList();\n    }\n}'
    },
    evidence: '이력서 프로젝트 「사내 게시판 API」의 근거로 연결됨', replies: 5, reposts: 12, likes: 74
  },
  {
    id: 7, type: 'repo', author: '박지훈', handle: 'jihoon_be', initial: '지', av: 'av5', time: '3시간',
    body: '3계층 구조로 만든 게시판 예제 레포 공개했어요. 구조 리뷰 환영합니다.',
    repo: { name: 'jihoon/spring-layered-board', desc: 'Controller · Service · Repository 3계층 게시판 예제', lang: 'Java', langColor: '#B07219', stars: 24, updated: '어제 업데이트' },
    evidence: '이력서 프로젝트 「사내 게시판 API」의 근거로 연결됨', replies: 3, reposts: 6, likes: 30
  },
  {
    id: 13, type: 'general', author: '한민서', handle: 'minseo_be', initial: '민', av: 'av6', time: '4시간',
    body: '1일 1커밋 365일 채웠습니다.\n잔디보다 중요한 건 커밋 메시지랑 PR 설명이라는 걸 배운 1년이었어요. 면접에서도 GitHub 링크 보고 질문 많이 받았어요.',
    image: githubGrass, imageAlt: 'GitHub 기여 잔디 1년치. 365일 연속 커밋, 올해 기여 1,284회',
    replies: 21, reposts: 18, likes: 342
  },
  {
    id: 14, type: 'tech', author: '정우진', handle: 'woojin_fe', initial: '우', av: 'av4', time: '4시간',
    body: 'CSS 변수 하나로 다크 모드 만들기\n색을 전부 --bg, --text 같은 토큰으로 빼두면 html의 data-theme 값만 바꿔서 끝나요. 컴포넌트 코드는 한 줄도 안 건드렸어요.',
    video: darkModeDemo, poster: darkModeDemoPoster, duration: '0:06',
    imageAlt: '프로필 화면에서 다크 모드 버튼을 눌러 라이트와 다크 테마를 오가는 화면 녹화',
    tags: ['React', 'TypeScript'], source: 'MDN · CSS 사용자 지정 속성', replies: 10, reposts: 37, likes: 176
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
    id: 4, type: 'job', jobId: 3, jobMeta: '마감 D-12', jobSub: 'Java · Spring Boot · MySQL · 서울 성수', company: true, author: '노바랩스', handle: 'novalabs', initial: 'N', av: 'co1', time: '5시간',
    body: '백엔드 개발자를 채용합니다. 공통 이력서로 바로 지원할 수 있어요.', replies: 2, reposts: 5, likes: 22
  },
  {
    id: 16, type: 'general', author: '김하윤', handle: 'hayun_fe', initial: '윤', av: 'av8', time: '7시간',
    body: '포트폴리오 README 템플릿 공유해요.\n프로젝트마다 "문제 → 해결 → 결과"를 따로 적었더니 면접에서 "그래서 뭘 했어요?" 질문이 확 줄었어요.',
    image: readmeTemplate, imageAlt: 'GitHub README 화면. 프로젝트 소개, 맡은 일, 한눈에 보기 표, 문제 해결 결과 목록',
    replies: 12, reposts: 64, likes: 230
  },
  {
    id: 17, type: 'question', threadId: 1, author: '김도윤', handle: 'doyoon_cs', initial: '도', av: 'av2', time: '8시간',
    body: '백엔드 신입 코딩테스트 언어, Java랑 Python 중에 뭐가 나을까요?\n실무는 Spring 쓸 거라 Java로 하는 게 맞나 고민이에요.',
    replies: 9, reposts: 1, likes: 15
  },
  {
    id: 18, type: 'general', company: true, author: '노바랩스', handle: 'novalabs', initial: 'N', av: 'co1', time: '9시간',
    body: '이번 주 금요일 오후 4시, 백엔드 신입 이력서 라이브 피드백 합니다.\n피드백 게시판에 "백엔드"로 요청 올려 주시면 몇 분 골라서 현직자들이 같이 봐요.',
    replies: 16, reposts: 29, likes: 95
  },
  {
    id: 5, type: 'job', jobId: 1, jobMeta: '요구 문항 2개 · 마감 D-5', jobSub: '회사가 요구하는 자기소개 문항에 맞춰 특화 이력서를 작성해 지원합니다.', company: true, author: '블루웨일 스튜디오', handle: 'bluewhale', initial: 'B', av: 'co2', time: '1일',
    body: '프론트엔드 인턴을 찾고 있어요. 저희가 궁금한 질문 두 가지에 답해주세요.', replies: 5, reposts: 11, likes: 37
  }
];

// 프로필 "게시글" 탭 첫 글. 피드 추천 탭에는 나오지 않는다.
export const PROFILE_POST = { time: '2시간', body: '프론트엔드 신입 이력서 피드백 부탁드려요. 프로젝트 설명이 너무 길지는 않은지 봐주세요.' };

// 공유자 배지: 실제로는 "태그별 기술 글 N개 이상" 규칙으로 서버에서 계산
export const SHARER_BADGES = { woojin_fe: 'React 공유자', minseo_be: 'Spring 공유자', jihoon_be: 'Java 공유자' };

// 리뷰어 인증 배지: 채택된 피드백 수로 분기마다 계산 (피드백 게시판에 표시)
export const REVIEWER_BADGES = { 박지훈: '백엔드 리뷰어', 한민서: '백엔드 우수 리뷰어', 정우진: '프론트엔드 우수 리뷰어' };

export const TECH_TAGS = ['React', 'TypeScript', 'Spring', 'Java', 'Docker', 'AI'];

// 공고 type: general(공통 이력서 제출) | special(요구 문항 + 특화 이력서)
// meta: 목록 카드 / detailMeta: 상세 / field: 공통 이력서 추천 기준
export const JOBS = [
  {
    id: 1, type: 'special', company: '블루웨일 스튜디오', handle: 'bluewhale', initial: 'B', av: 'co2',
    title: '프론트엔드 인턴 (React · TypeScript)', role: '프론트엔드 인턴', dday: 'D-5', employment: '인턴 3개월',
    meta: '서울 마포 · 인턴 3개월 · 요구 문항 2개', detailMeta: '서울 마포 · 주 5일 · 정규직 전환 검토',
    intro: '사용자가 직접 쓰는 웹 서비스의 화면을 함께 만들 인턴을 찾습니다. 디자이너, 백엔드 개발자와 매일 같이 일하며 실제 배포까지 경험하게 됩니다.',
    requirements: ['React로 만든 프로젝트 경험', 'Git으로 협업해 본 경험', 'HTML · CSS 기본기'],
    preferred: ['TypeScript 사용 경험', '디자인 시스템 이해', 'GitHub 활동 기록'],
    questions: [
      { text: '가장 어려웠던 UI 문제와 해결 과정을 알려주세요.', limit: 500, placeholder: '문제 → 시도한 방법 → 결과 순서로 써 보세요.', hint: '공통 이력서의 프로젝트 내용을 참고해 작성할 수 있어요' },
      { text: '블루웨일 서비스 중 하나를 골라 개선점을 제안해 주세요.', limit: 800, placeholder: '어떤 화면의 무엇을, 왜, 어떻게 바꾸고 싶은지 적어 주세요.' }
    ],
    otherJobs: [{ to: '/jobs/5', label: 'UI 디자이너 인턴 · 일반' }, { to: '/jobs', label: '백엔드 주니어 · 특화' }]
  },
  {
    id: 2, type: 'general', company: '코어브릿지', handle: 'corebridge', initial: 'C', av: 'co3', field: '백엔드',
    title: '데이터 엔지니어', dday: 'D-7', employment: '정규직',
    meta: '서울 강남 · 정규직 · 경력 1~3년 · Python · Airflow', detailMeta: '서울 강남 · 주 5일 · Python · Airflow',
    intro: '서비스 데이터를 모으고 가공하는 파이프라인을 함께 만들 데이터 엔지니어를 찾습니다.',
    requirements: ['Python 프로젝트 경험', 'SQL 기본기', 'Git으로 협업해 본 경험'],
    preferred: ['Airflow 사용 경험', '데이터 모델링 경험']
  },
  {
    id: 3, type: 'general', company: '노바랩스', handle: 'novalabs', initial: 'N', av: 'co1', field: '백엔드',
    title: '백엔드 개발자 (신입 · 경력 1~3년)', dday: 'D-12', employment: '정규직',
    meta: '서울 성수 · 정규직 · Java · Spring Boot · MySQL', detailMeta: '서울 성수 · 주 5일 · Java · Spring Boot · MySQL',
    intro: '하루 수백만 건의 결제 데이터를 다루는 API 서버를 함께 만들 백엔드 개발자를 찾습니다. 코드 리뷰와 테스트를 중요하게 생각하는 팀이에요.',
    requirements: ['Java · Spring Boot 프로젝트 경험', 'RDB 설계와 SQL 기본기', 'Git으로 협업해 본 경험'],
    preferred: ['JPA 성능 문제 해결 경험', 'Docker · CI/CD 경험', '기술 블로그 · GitHub 활동']
  },
  {
    id: 4, type: 'special', company: '모아핀', handle: 'moafin', initial: 'M', av: 'co4',
    title: '주니어 프론트엔드', role: '주니어 프론트엔드', dday: 'D-15', employment: '정규직',
    meta: '판교 · 정규직 · 요구 문항 1개', detailMeta: '판교 · 주 5일 · 정규직',
    intro: '금융 데이터를 쉽게 보여 주는 화면을 함께 만들 주니어를 찾습니다.',
    requirements: ['React 실무 또는 프로젝트 경험'], preferred: ['차트 라이브러리 경험'],
    questions: [{ text: '데이터를 시각화하며 가장 신경 쓴 점을 알려주세요.', limit: 600, placeholder: '문제 → 시도한 방법 → 결과 순서로 써 보세요.' }]
  },
  {
    id: 5, type: 'general', company: '블루웨일 스튜디오', handle: 'bluewhale', initial: 'B', av: 'co2', field: '프론트엔드',
    title: 'UI 디자이너 인턴', dday: 'D-20', employment: '인턴 3개월',
    meta: '서울 마포 · 인턴 3개월 · Figma', detailMeta: '서울 마포 · 주 5일 · Figma',
    intro: '서비스 화면과 디자인 시스템을 함께 다듬을 UI 디자이너 인턴을 찾습니다.',
    requirements: ['Figma 사용 경험', '포트폴리오 제출 가능'],
    preferred: ['디자인 시스템 이해', 'HTML · CSS 이해']
  }
];

// 공통 이력서는 용도별로 여러 개 만들 수 있다. (내 이력서 · 공고 지원 · 프로필 공개 설정이 함께 쓴다)
export const COMMON_RESUMES = [
  {
    id: 1, name: '프론트엔드 개발자 · 이서연', field: '프론트엔드', sections: '학력 · 기술 스택 · 프로젝트 3개 · 수상 1개', updated: '3일 전 수정', feedback: 12, bases: 2,
    skills: ['React', 'TypeScript', 'Next.js', 'Git'],
    projects: [{ title: '스터디 매칭 웹앱', desc: 'React · 무한 스크롤 · 팀 4명' }, { title: '디자인 시스템 컴포넌트', desc: 'TypeScript · 개인' }]
  },
  {
    id: 2, name: '백엔드 지원용', field: '백엔드', sections: '학력 · 기술 스택 · 프로젝트 2개', updated: '1주 전 수정', feedback: 5, bases: 0,
    skills: ['Java', 'Spring Boot', 'MySQL', 'Docker'],
    projects: [{ title: '사내 게시판 API', desc: 'Spring · 3계층 · JPA' }, { title: '스터디 매칭 웹앱', desc: '백엔드 API 일부 담당' }]
  },
  {
    id: 3, name: '인턴 지원용 (1장 요약)', field: '프론트엔드', sections: '기술 스택 · 프로젝트 1개', updated: '2주 전 수정', feedback: 0, bases: 1,
    skills: ['React', 'TypeScript'],
    projects: [{ title: '스터디 매칭 웹앱', desc: 'React · 무한 스크롤' }]
  }
];

// 프로필에 공개한 공통 이력서 (체크한 순서대로 보인다)
export const PUBLIC_RESUME_IDS = [1];
export const MAX_PUBLIC_RESUMES = 3;

// 특화 이력서 작성 화면에서 불러오는 공통 이력서 내용
export const COMMON_RESUME = {
  title: '프론트엔드 개발자 · 이서연',
  basic: { name: '이서연', school: '[학교명] 컴퓨터공학과', github: 'github.com/[아이디]' },
  skills: ['React', 'TypeScript', 'Next.js', 'Tailwind', 'Git'],
  projects: [
    { id: 'p1', title: '스터디 매칭 웹앱', desc: 'React · 팀 4명 · 검색 필터와 무한 스크롤 구현', evidence: [{ type: 'repo', label: 'study-match-web · TypeScript' }, { type: 'blog', label: '벨로그 · 무한 스크롤 구현기' }] },
    { id: 'p2', title: '디자인 시스템 컴포넌트 라이브러리', desc: 'TypeScript · 개인 · 버튼 · 모달 등 12개 컴포넌트', evidence: [{ type: 'repo', label: 'button-kit · TypeScript' }], canAttach: true },
    { id: 'p3', title: '알고리즘 풀이 블로그', desc: 'Next.js · 개인' }
  ]
};

export const SPECIAL_RESUMES = [
  { id: 1, jobId: 1, initial: 'B', company: '블루웨일 스튜디오', role: '프론트엔드 인턴', base: '공통 이력서 기반', questions: 2, updated: '방금 전', status: 'writing' },
  { id: 2, jobId: null, initial: 'C', company: '코어브릿지', role: '웹 개발 인턴', base: '처음부터 작성', questions: 3, updated: '2일 전', status: 'submitted' },
  { id: 3, jobId: 4, initial: 'M', company: '모아핀', role: '주니어 프론트엔드', base: '공통 이력서 기반', questions: 1, updated: '1주 전', status: 'submitted' }
];

// 피드백 게시판에 올라온 이력서. adopted: 채택된 피드백의 순번
export const FEEDBACK_REQUESTS = [
  {
    id: 1, mine: true, author: ME.name, handle: ME.handle, initial: ME.initial, av: ME.av, time: '2시간', field: '프론트엔드',
    resumeType: '공통 이력서', resumeTitle: '프론트엔드 개발자 · 이서연', sections: '학력 · 기술 스택 · 프로젝트 3개 · 수상 1개',
    message: '프론트엔드 신입 이력서예요. 프로젝트 설명이 너무 길지는 않은지, 기술 스택 배치가 괜찮은지 봐주세요.', focus: ['프로젝트', '기술 스택'],
    feedbacks: [
      { id: 'f3', author: '박지훈', initial: '지', av: 'av5', section: '프로젝트', time: '30분', text: '프로젝트마다 "문제 → 해결 → 결과" 순서로 3줄씩만 정리해도 훨씬 잘 읽혀요.' },
      { id: 'f4', author: '최하은', initial: '하', av: 'av3', section: '기술 스택', time: '10분', text: '숙련도 높은 기술을 앞에 두고, 써 본 정도인 건 "경험 있음"으로 따로 묶는 걸 추천해요.' }
    ]
  },
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
    id: 4, author: '한민서', handle: 'minseo_be', initial: '민', av: 'av6', time: '1일', field: '백엔드', adopted: 0,
    resumeType: '공통 이력서', resumeTitle: '백엔드 개발자 · 한민서', sections: '학력 · 기술 스택 · 프로젝트 4개',
    message: 'Spring 프로젝트 두 개 중 어떤 걸 대표로 둘지 고민이에요.', focus: ['프로젝트'],
    feedbacks: [
      { id: 'f2', author: '박지훈', initial: '지', av: 'av5', section: '프로젝트', time: '20시간', text: '테스트 코드와 배포까지 해 본 쪽을 대표로 두세요. 면접에서 이야기할 거리가 훨씬 많아요.' }
    ]
  }
];

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

export const SIMILAR_QUESTIONS = [
  { id: 1, answers: 9, title: '이력서에 GitHub 링크만 넣어도 될까요?' },
  { id: 2, answers: 3, title: '노션 포트폴리오 vs 개인 사이트' }
];

// 리뷰어 인증 배지 진행 현황 (프로필)
export const BADGE_PROGRESS = {
  goal: '프론트엔드 리뷰어',
  nextEval: '2027년 1월 1일',
  dday: 'D-63',
  metrics: [
    { label: '채택된 피드백', value: 3, max: 5, unit: '건' },
    { label: '서로 다른 요청자', value: 2, max: 3, unit: '명' }
  ],
  remaining: '채택 2건, 새 요청자 1명이 더 필요해요.'
};

// 인재 찾기 (기업 회원 전용). 배지 status: active | grace(갱신 필요) | expired(지난 배지)
export const TALENTS = [
  { id: 1, name: '한민서', handle: 'minseo_be', initial: '민', av: 'av6', field: '백엔드', career: '1~3년', headline: 'Docker 이미지 85% 줄인 Spring 백엔드 개발자', resumeTitle: '백엔드 지원용', skills: ['Java', 'Spring', 'Docker', 'MySQL'], badges: [{ label: '백엔드 우수 리뷰어', status: 'active', streak: 3, since: '2026 Q2' }], github: 1284, posts: 9, region: '서울', updated: '오늘', recent: 18, requesters: 11, best: 3, projects: [['배포 자동화', '빌드·실행 스테이지 분리로 배포 3분 40초 → 52초'], ['주문 API', '트래픽 피크 시 응답 1.2초 → 0.3초']] },
  { id: 2, name: '정우진', handle: 'woojin_fe', initial: '우', av: 'av4', field: '프론트엔드', career: '1~3년', headline: '캐시 설계까지 챙기는 React 개발자', resumeTitle: '프론트엔드 개발자', skills: ['React', 'TypeScript', 'React Query'], badges: [{ label: '프론트엔드 우수 리뷰어', status: 'active', streak: 2, since: '2026 Q3' }], github: 812, posts: 12, region: '서울', updated: '어제', recent: 16, requesters: 9, best: 2, projects: [['스터디 매칭 웹앱', '무한 스크롤 캐시 문제 해결, 재요청 60% 감소'], ['디자인 시스템', 'CSS 변수 토큰으로 다크 모드 적용']] },
  { id: 3, name: '박지훈', handle: 'jihoon_be', initial: '지', av: 'av5', field: '백엔드', career: '신입', headline: '3계층 구조를 지키는 신입 Java 개발자', resumeTitle: '사내 게시판 API', skills: ['Java', 'Spring', 'JPA'], badges: [{ label: '백엔드 리뷰어', status: 'active', streak: 1, since: '2026 Q4' }], github: 456, posts: 7, region: '경기', updated: '2일 전', recent: 6, requesters: 4, best: 0, projects: [['사내 게시판 API', '컨트롤러 로직을 서비스로 분리, 테스트 커버리지 72%']] },
  { id: 4, name: '이서연', handle: 'seoyeon_dev', initial: '서', av: 'av1', field: '프론트엔드', career: '신입', headline: '사용자 피드백으로 화면을 고치는 프론트엔드 개발자', resumeTitle: '프론트엔드 개발자 · 이서연', skills: ['React', 'TypeScript', 'Next.js'], badges: [], github: 389, posts: 3, region: '서울', updated: '오늘', recent: 0, requesters: 0, best: 0, projects: [['SNS 클론', '피드 탭 고정, 기술 태그 필터 구현'], ['포트폴리오 사이트', 'Lighthouse 성능 점수 62 → 95']] },
  { id: 5, name: '김도윤', handle: 'doyoon_cs', initial: '도', av: 'av2', field: '백엔드', career: '신입', headline: 'N+1 문제를 직접 겪고 고친 백엔드 지망생', resumeTitle: '백엔드 개발자', skills: ['Java', 'Spring', 'JPA', 'Redis'], badges: [], github: 214, posts: 1, region: '대전', updated: '3일 전', recent: 0, requesters: 0, best: 0, projects: [['피드 서비스', '쿼리 21번 → 2번으로 줄인 조회 최적화']] },
  { id: 6, name: '최하은', handle: 'haeun_ux', initial: '하', av: 'av3', field: '프론트엔드', career: '신입', headline: 'UX를 데이터로 검증하는 프론트엔드 개발자', resumeTitle: 'UX 중심 포트폴리오', skills: ['React', 'JavaScript', 'Figma'], badges: [{ label: '프론트엔드 리뷰어', status: 'grace', streak: 0, since: '2026 Q2', until: '2026 Q3' }], github: 302, posts: 4, region: '부산', updated: '1주 전', recent: 3, requesters: 2, best: 1, projects: [['트위터 클론', '탭 고정과 태그 필터로 체류 시간 1.4배']] },
  { id: 7, name: '김하윤', handle: 'hayun_fe', initial: '윤', av: 'av8', field: '풀스택', career: '3년 이상', headline: 'README로 문제 해결을 보여 주는 풀스택 개발자', resumeTitle: '풀스택 경력 이력서', skills: ['TypeScript', 'Node.js', 'React', 'PostgreSQL'], badges: [{ label: '풀스택 우수 리뷰어', status: 'active', streak: 5, since: '2025 Q4' }], github: 1730, posts: 15, region: '서울', updated: '어제', recent: 22, requesters: 14, best: 5, projects: [['결제 대시보드', '정산 리포트 생성 10분 → 40초'], ['README 템플릿', '팀 30곳이 사용하는 템플릿 공개']] },
  { id: 8, name: '이준호', handle: 'junho_be', initial: '준', av: 'av7', field: '백엔드', career: '1~3년', headline: 'ERD부터 설계하는 백엔드 개발자', resumeTitle: '데이터 모델링 이력서', skills: ['Kotlin', 'Spring', 'PostgreSQL'], badges: [{ label: '백엔드 리뷰어', status: 'expired', streak: 0, since: '2026 Q1', until: '2026 Q2' }], github: 640, posts: 2, region: '경기', updated: '5일 전', recent: 1, requesters: 1, best: 0, projects: [['피드백 서비스 ERD', 'CHECK 제약으로 이력서 참조 무결성 보장']] },
  { id: 9, name: '오지민', handle: 'jimin_full', initial: '오', av: 'av9', field: '풀스택', career: '1~3년', headline: '혼자서 서비스를 끝까지 배포해 본 개발자', resumeTitle: '1인 서비스 운영 경험', skills: ['Python', 'Django', 'React', 'AWS'], badges: [], github: 905, posts: 6, region: '서울', updated: '4일 전', recent: 0, requesters: 0, best: 0, projects: [['가계부 앱', '월 사용자 1,200명 서비스 1년 운영']] },
  { id: 10, name: '윤태경', handle: 'taekyung_be', initial: '태', av: 'av10', field: '백엔드', career: '3년 이상', headline: '대용량 트래픽을 다뤄 본 백엔드 개발자', resumeTitle: '경력 4년 백엔드', skills: ['Java', 'Spring', 'Kafka', 'Redis'], badges: [{ label: '백엔드 우수 리뷰어', status: 'active', streak: 4, since: '2026 Q1' }], github: 1102, posts: 8, region: '서울', updated: '오늘', recent: 17, requesters: 10, best: 4, projects: [['이벤트 파이프라인', 'Kafka로 주문 이벤트 초당 3,000건 처리'], ['캐시 계층', 'Redis 도입으로 DB 부하 45% 감소']] }
];

export const TALENT_SKILLS = ['Java', 'Spring', 'React', 'TypeScript', 'Kotlin', 'Redis', 'Docker', 'Python'];

export const REPOS = [
  { name: 'study-match-web', lang: 'TypeScript' },
  { name: 'button-kit', lang: 'TypeScript' },
  { name: 'algo-blog', lang: 'JavaScript' },
  { name: 'dotfiles', lang: 'Shell' }
];

// 블로그 플랫폼별 RSS 주소 규칙 (백엔드 RSS 수집기와 같은 규칙)
export const RSS_RULES = {
  velog: { label: '벨로그', input: '블로그 아이디', url: (id) => `https://v2.velog.io/rss/@${id || '[아이디]'}` },
  tistory: { label: '티스토리', input: '블로그 아이디', url: (id) => `https://${id || '[아이디]'}.tistory.com/rss` },
  etc: { label: '기타 블로그', input: 'RSS 주소', url: (v) => v || '[RSS 주소]' }
};
