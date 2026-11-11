#!/usr/bin/env node
import { createInterface } from 'node:readline/promises';
import { stdin as input, stdout as output } from 'node:process';
import { authenticatedFetch, clearSession, currentSession, login } from './session.js';

type Options = Record<string, string>;

function parseOptions(args: string[]): { positional: string[]; options: Options } {
  const positional: string[] = [];
  const options: Options = {};
  for (let index = 0; index < args.length; index += 1) {
    const arg = args[index];
    if (!arg.startsWith('--')) {
      positional.push(arg);
      continue;
    }
    const [key, inlineValue] = arg.slice(2).split('=', 2);
    const value = inlineValue ?? args[++index];
    if (!value || value.startsWith('--')) throw new Error(`옵션 값이 필요합니다: --${key}`);
    options[key] = value;
  }
  return { positional, options };
}

function listOption(value: string | undefined): string[] | undefined {
  return value?.split(',').map((item) => item.trim()).filter(Boolean);
}

function usage(): void {
  console.log(`사용법:
  resuit login
  resuit logout
  resuit whoami
  resuit post "제목" [--hashtag a,b]
  resuit ask "제목" [--hashtag a,b]
  resuit tech "제목" [--tech React,TypeScript] [--hashtag a,b]
  resuit code "제목" --repo owner/project [--file example.ts] [--language typescript] [--code "const value = 1;"]`);
}

async function promptLogin(): Promise<void> {
  const rl = createInterface({ input, output });
  try {
    const username = await rl.question('아이디: ');
    const password = await promptPassword(rl);
    const session = await login(username.trim(), password);
    console.log(`${session.user?.nickname ?? session.user?.username}님, 로그인되었습니다.`);
  } finally {
    rl.close();
  }
}

async function promptPassword(rl: ReturnType<typeof createInterface>): Promise<string> {
  if (!input.isTTY || !input.setRawMode) return rl.question('비밀번호: ');
  output.write('비밀번호: ');
  input.setRawMode(true);
  input.resume();
  return new Promise((resolve, reject) => {
    let password = '';
    const onData = (chunk: Buffer): void => {
      for (const byte of chunk) {
        if (byte === 3) {
          cleanup();
          reject(new Error('로그인이 취소되었습니다.'));
        } else if (byte === 13 || byte === 10) {
          cleanup();
          output.write('\n');
          resolve(password);
        } else if (byte === 127) {
          password = password.slice(0, -1);
        } else {
          password += String.fromCharCode(byte);
        }
      }
    };
    const cleanup = (): void => {
      input.setRawMode?.(false);
      input.removeListener('data', onData);
    };
    input.on('data', onData);
  });
}

function codeContent(title: string, options: Options): string {
  const code = options.code;
  if (!code) return title;
  const file = options.file || 'snippet.txt';
  const language = options.language || file.split('.').pop() || 'text';
  const fence = '```';
  return `${title}\n\n${fence}${language} filename=${encodeURIComponent(file)}\n${code}\n${fence}`;
}

async function createPost(command: string, args: string[]): Promise<void> {
  const { positional, options } = parseOptions(args);
  const title = positional.join(' ').trim();
  if (!title) throw new Error('게시글 제목을 입력해 주세요.');
  const type = command === 'ask' ? 'QUESTION' : command === 'tech' ? 'TECH' : 'NORMAL';
  if (command === 'code' && !options.repo) throw new Error('코드 글에는 --repo owner/project가 필요합니다.');
  const body = {
    type: command === 'code' ? 'NORMAL' : type,
    content: command === 'code' ? codeContent(title, options) : title,
    repoFullName: options.repo,
    techTags: listOption(options.tech),
    hashtags: listOption(options.hashtag),
  };
  const response = await authenticatedFetch('/api/v1/posts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(`게시글 작성에 실패했습니다. (${response.status}) ${message}`);
  }
  const post = await response.json() as { id: number };
  console.log(`게시글을 작성했습니다. (id: ${post.id})`);
}

async function main(): Promise<void> {
  const [command, ...args] = process.argv.slice(2);
  switch (command) {
    case 'login': await promptLogin(); break;
    case 'logout': await clearSession(); console.log('로그아웃되었습니다.'); break;
    case 'whoami': {
      const session = await currentSession();
      console.log(`${session.user?.nickname ?? session.user?.username} (${session.user?.username})`);
      break;
    }
    case 'post':
    case 'ask':
    case 'tech':
    case 'code':
      await createPost(command, args);
      break;
    case undefined:
    case 'help':
    case '--help':
      usage();
      break;
    default:
      throw new Error(`알 수 없는 명령입니다: ${command}`);
  }
}

main().catch((error: unknown) => {
  console.error(error instanceof Error ? error.message : error);
  process.exitCode = 1;
});
