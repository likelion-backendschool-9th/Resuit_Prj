import { chmod, mkdir, readFile, unlink, writeFile } from 'node:fs/promises';
import { dirname } from 'node:path';
import { API_BASE_URL, SESSION_FILE } from './config.js';

type Session = {
  cookie: string;
  csrfHeader: string | null;
  csrfToken: string | null;
  user: { username: string; nickname: string } | null;
};

async function save(session: Session): Promise<void> {
  await mkdir(dirname(SESSION_FILE), { recursive: true, mode: 0o700 });
  await writeFile(SESSION_FILE, `${JSON.stringify(session, null, 2)}\n`, { mode: 0o600 });
  await chmod(SESSION_FILE, 0o600);
}

export async function loadSession(): Promise<Session | null> {
  try {
    return JSON.parse(await readFile(SESSION_FILE, 'utf8')) as Session;
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === 'ENOENT') return null;
    throw new Error(`로그인 세션을 읽을 수 없습니다: ${SESSION_FILE}`);
  }
}

export async function clearSession(): Promise<void> {
  try {
    await unlink(SESSION_FILE);
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code !== 'ENOENT') throw error;
  }
}

function cookieHeader(response: Response, previous = ''): string {
  const cookies = response.headers.getSetCookie?.() ?? [];
  const values = new Map<string, string>();
  for (const value of previous.split('; ').filter(Boolean)) {
    const [name, ...parts] = value.split('=');
    values.set(name, parts.join('='));
  }
  for (const cookie of cookies) {
    const [pair] = cookie.split(';', 1);
    const [name, ...value] = pair.split('=');
    values.set(name, value.join('='));
  }
  return [...values].map(([name, value]) => `${name}=${value}`).join('; ');
}

async function requestSession(cookie = ''): Promise<Session> {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/session`, {
    headers: cookie ? { Cookie: cookie } : undefined,
  });
  if (!response.ok) throw new Error(`세션 확인에 실패했습니다. (${response.status})`);
  const body = await response.json() as {
    authenticated: boolean;
    user: Session['user'];
    csrfHeader: string | null;
    csrfToken: string | null;
  };
  return {
    cookie: cookieHeader(response, cookie),
    csrfHeader: body.csrfHeader,
    csrfToken: body.csrfToken,
    user: body.authenticated ? body.user : null,
  };
}

export async function login(username: string, password: string): Promise<Session> {
  let session = await requestSession();
  const response = await fetch(`${API_BASE_URL}/api/v1/users/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(session.cookie ? { Cookie: session.cookie } : {}),
      ...(session.csrfHeader && session.csrfToken ? { [session.csrfHeader]: session.csrfToken } : {}),
    },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) {
    if (response.status === 401) throw new Error('아이디 또는 비밀번호를 확인해 주세요.');
    throw new Error(`로그인에 실패했습니다. (${response.status})`);
  }
  session = await requestSession(cookieHeader(response, session.cookie));
  if (!session.user) throw new Error('로그인 응답에 사용자 정보가 없습니다.');
  await save(session);
  return session;
}

export async function currentSession(): Promise<Session> {
  const session = await loadSession();
  if (!session) throw new Error('로그인되어 있지 않습니다. 먼저 resuit login을 실행해 주세요.');
  const refreshed = await requestSession(session.cookie);
  if (!refreshed.user) {
    await clearSession();
    throw new Error('로그인 세션이 만료되었습니다. 다시 로그인해 주세요.');
  }
  await save(refreshed);
  return refreshed;
}

export async function authenticatedFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const session = await currentSession();
  const headers = new Headers(init.headers);
  headers.set('Cookie', session.cookie);
  if (session.csrfHeader && session.csrfToken && init.method && init.method !== 'GET') {
    headers.set(session.csrfHeader, session.csrfToken);
  }
  return fetch(`${API_BASE_URL}${path}`, { ...init, headers });
}
