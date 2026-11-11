# Resuit CLI

Resuit 백엔드의 세션 로그인과 게시글 작성 CLI입니다.

## 설치 및 실행

```bash
cd cli
npm install
npm run build
npm link
resuit login
```

## API 주소 설정

CLI는 [config.ts](./src/config.ts)의 `RESUIT_API_URL` 환경변수로 호출할 백엔드 주소를 결정합니다.
환경변수가 없으면 기본값으로 `http://localhost:8080`을 사용합니다.

로컬 개발환경에서 cli사용을 위해선 아래의 명령어

```bash
export RESUIT_API_URL=http://localhost:8080
```

운영환경에서 cli사용을 위해선 아래의 명령어

```bash
export RESUIT_API_URL=https://resuit.newlecture.com
```

매번 설정하지 않으려면 사용하는 셸 설정 파일에 추가합니다.

```bash
echo 'export RESUIT_API_URL=https://resuit.newlecture.com' >> ~/.zshrc
source ~/.zshrc
```

설정된 주소를 확인한 뒤 로그인합니다.

```bash
echo "$RESUIT_API_URL"
resuit login
```

```bash
resuit login
resuit logout
resuit whoami
resuit post "Next.js에서 배운 점"
resuit ask "Spring Security 세션이 궁금해요" --hashtag Java,Spring
resuit tech "React 상태 관리 비교" --tech React,TypeScript --hashtag frontend,nextjs
resuit code "Redis 캐싱 적용기" --repo owner/project --file cache.ts --language typescript --code "const value = await cache.get(key);"
```

`npm link`를 사용하지 않으려면 빌드 후 직접 실행할 수 있습니다.

```bash
node dist/index.js whoami
```

업데이트한 뒤에는 `npm run build`를 다시 실행합니다.

## 명령 및 옵션

### 인증

| 명령 | 설명 |
|---|---|
| `resuit login` | 아이디와 비밀번호로 로그인하고 세션을 저장합니다. |
| `resuit logout` | 저장된 CLI 로그인 세션을 삭제합니다. |
| `resuit whoami` | 현재 로그인한 사용자를 확인합니다. |

### 게시글 작성 명령

| 명령 | 게시글 타입 | 설명 |
|---|---|---|
| `resuit post "제목"` | 일반 글 (`NORMAL`) | 일반 게시글을 작성합니다. |
| `resuit ask "제목"` | 질문 (`QUESTION`) | 질문 게시글을 작성합니다. |
| `resuit tech "제목"` | 기술 정보 (`TECH`) | 기술 정보 게시글을 작성합니다. |
| `resuit code "제목"` | 일반 글 + 코드 | 코드 블록과 저장소를 포함한 게시글을 작성합니다. |

### 게시글 옵션

| 옵션 | 적용 명령 | 설명 |
|---|---|---|
| `--hashtag a,b` | `post`, `ask`, `tech`, `code` | 게시글에 해시태그를 쉼표로 구분해 추가합니다. |
| `--tech React,TypeScript` | `tech` | 기술 태그를 최대 3개까지 추가합니다. 지원 태그는 `React`, `TypeScript`, `Spring`, `Java`, `Docker`, `AI`입니다. |
| `--repo owner/project` | `code` | GitHub 저장소를 게시글에 연결합니다. |
| `--file example.ts` | `code` | 코드 블록의 파일명을 지정합니다. 기본값은 `snippet.txt`입니다. |
| `--language typescript` | `code` | 코드 블록의 언어를 지정합니다. |
| `--code "const value = 1;"` | `code` | 코드 블록의 내용을 입력합니다. |

해시태그는 최대 10개까지 입력할 수 있으며, 기술 태그는 기술 게시글에서만 저장됩니다. `--code`는 현재 한 줄 인자 방식이므로 여러 줄 코드는 셸의 문자열 문법을 사용하거나 파일 입력 기능이 필요합니다.

## 삭제

전역으로 연결한 CLI 명령을 제거하려면 다음을 실행합니다.

```bash
npm unlink --global @resuit/cli
```

그래도 제거되지 않으면 같은 효과의 전역 패키지 삭제 명령을 사용합니다.

```bash
npm uninstall --global @resuit/cli
```

프로젝트 의존성과 빌드 결과까지 삭제하려면 다음을 실행합니다.

```bash
rm -rf node_modules dist
```

로그인 세션 파일도 삭제하려면 다음을 실행합니다.

```bash
rm -f ~/.config/resuit/session.json
```

현재 백엔드에는 CLI 전용 OAuth 브라우저 콜백이 없으므로 `login`은 기존 JSON 로그인 API를 사용합니다. 비밀번호는 저장하지 않고 세션 쿠키와 CSRF 토큰만 `~/.config/resuit/session.json`에 권한 제한으로 저장합니다.
