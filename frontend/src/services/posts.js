// PostTimelineResponse를 기존 피드 카드의 표시 모델로 변환한다.
function toPost(post) {
  const author = post.author.nickname || post.author.username;
  return {
    id: post.id,
    author,
    handle: post.author.username,
    initial: author?.slice(0, 1) || '?',
    profileImage: post.author.profileImageUrl,
    type: post.type,
    body: post.content,
    image: post.imageUrl,
    imageAlt: `${author}님의 첨부 이미지`,
    video: post.videoUrl,
    blogPostUrl: safeUrl(post.blogPostUrl),
    repoUrl: post.repoFullName
      ? `https://github.com/${post.repoFullName.split('/').map(encodeURIComponent).join('/')}`
      : null,
    repoFullName: post.repoFullName,
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
