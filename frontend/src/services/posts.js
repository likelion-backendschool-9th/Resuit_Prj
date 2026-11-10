// PostTimelineResponse를 기존 피드 카드의 표시 모델로 변환한다.
function toPost(post) {
  const author = post.author.nickname || post.author.username;
  return {
    id: post.id,
    liked: post.liked === true,
    author,
    handle: post.author.username,
    initial: author?.slice(0, 1) || '?',
    profileImage: post.author.profileImageUrl,
    type: post.type?.toLowerCase(),
    body: post.content,
    techTags: post.techTags || [],
    hashtags: post.hashtags || [],
    // 예전 목업·화면 호환. 기술 태그만 tags로도 노출한다.
    tags: post.techTags || [],
    image: post.imageUrl,
    imageAlt: `${author}님의 첨부 이미지`,
    video: post.videoUrl,
    blogPostUrl: safeUrl(post.blogPostUrl),
    repoUrl: post.repoFullName
      ? `https://github.com/${post.repoFullName.split('/').map(encodeURIComponent).join('/')}`
      : null,
    repoFullName: post.repoFullName,
    commentCount: post.commentCount ?? 0,
    likeCount: post.likeCount ?? 0,
    time: post.createdAt?.replace('T', ' ').slice(0, 16) || '',
    source: 'api',
  };
}

function safeUrl(value) {
  try {
    const url = new URL(value);
    return ['https:', 'http:'].includes(url.protocol) ? url.href : null;
  } catch {
    return null;
  }
}

export async function getPosts({ page = 0, baseUrl = '' } = {}) {
  const response = await fetch(`${baseUrl}/api/v1/posts?page=${page}&size=20`, {
    credentials: 'same-origin',
    cache: 'no-store',
    signal: AbortSignal.timeout(10000),
  });
  if (!response.ok) throw new Error('게시글을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  const data = await response.json();
  return { posts: data.content.map(toPost), page: data.number, last: data.last };
}

async function mutatePost(path, method, body) {
  const { authenticatedFetch } = await import('./auth.js');
  const response = await authenticatedFetch(`/api/v1/posts${path}`, {
    method,
    ...(body ? { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) } : {}),
  });
  if (!response.ok) {
    if (response.status === 401) throw new Error('로그인이 필요해요. 다시 로그인해 주세요.');
    if (response.status === 403) throw new Error('처리 권한이 없거나 인증이 만료되었어요. 새로고침 후 다시 시도해 주세요.');
    if (response.status === 404) throw new Error('게시글을 찾을 수 없어요. 새로고침해 주세요.');
    const message = await response.text().catch(() => '');
    throw new Error(message || '게시글을 저장하거나 삭제하지 못했어요. 다시 시도해 주세요.');
  }
  return method === 'DELETE' ? null : toPost(await response.json());
}

const postPayload = (body) => ({
  type: body.type?.toUpperCase(),
  content: body.content,
  imageUrl: body.imageUrl,
  videoUrl: body.videoUrl,
  repoFullName: body.repoFullName,
  techTags: body.techTags ?? body.tags ?? null,
  hashtags: body.hashtags ?? null,
});
export const createPost = (body) => mutatePost('', 'POST', postPayload(body));
export const updatePost = (id, body) => mutatePost(`/${encodeURIComponent(id)}`, 'PATCH', postPayload(body));
export const deletePost = (id) => mutatePost(`/${encodeURIComponent(id)}`, 'DELETE');

export async function getPostDetail(id, { baseUrl = '', headers } = {}) {
  const response = await fetch(`${baseUrl}/api/v1/posts/${encodeURIComponent(id)}`, {
    cache: 'no-store', credentials: 'same-origin', headers, signal: AbortSignal.timeout(10000),
  });
  if (!response.ok) {
    throw new Error(response.status === 404
      ? '게시글을 찾을 수 없어요.'
      : '게시글을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
  return toPost(await response.json());
}


export async function getFeed({ tab = 'recommend', userId, tag, page = 0, baseUrl = '', signal, headers } = {}) {
  const query = new URLSearchParams({ tab, page: String(page), size: '20' });
  if ((tab === 'tech' || tab === 'hashtag') && tag && tag !== 'all') query.set('tag', tag);
  if (tab === 'following') {
    if (!userId) throw new Error('팔로잉 피드는 로그인 후 볼 수 있어요.');
    query.set('userId', userId);
  }
  const timeout = AbortSignal.timeout(10000);
  const response = await fetch(`${baseUrl}/api/v1/posts/feeds?${query}`, {
    credentials: 'same-origin', cache: 'no-store', headers,
    signal: signal ? AbortSignal.any([signal, timeout]) : timeout,
  });
  if (!response.ok) throw new Error('피드를 불러오지 못했어요. 다시 시도해 주세요.');
  const data = await response.json();
  return { posts: data.content.map(toPost), page: data.number, last: data.last };
}


async function setPostLike(id, liked) {
  const { authenticatedFetch } = await import('./auth.js');
  const response = await authenticatedFetch(`/api/v1/posts/${encodeURIComponent(id)}/likes`, { method: liked ? 'POST' : 'DELETE' });
  if (!response.ok) {
    if (response.status === 401) throw new Error('로그인이 필요해요. 다시 로그인해 주세요.');
    throw new Error('좋아요를 변경하지 못했어요. 다시 시도해 주세요.');
  }
}
export const likePost = (id) => setPostLike(id, true);
export const unlikePost = (id) => setPostLike(id, false);
