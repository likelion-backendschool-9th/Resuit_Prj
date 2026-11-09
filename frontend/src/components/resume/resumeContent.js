// 공통·특화 이력서 content(docs/database-design.md)를 화면에서 다룰 때 함께 쓰는 값과 함수

export const MAX_PROJECTS = 20;
export const MAX_AWARDS = 20;

// 특화 이력서가 프로젝트를 고를 때 id로 구분하므로 새 프로젝트에도 id를 붙인다
export const newProject = () => ({ id: `p-${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`, title: '', desc: '' });
export const newAward = () => ({ title: '', date: '' });

// 예전 데이터의 문자열 수상은 { title, date } 모양으로 맞춘다
export const normalizeContent = (content) => ({
  ...content,
  ...(Array.isArray(content.awards) && { awards: content.awards.map((a) => (typeof a === 'string' ? { title: a, date: '' } : a)) })
});

const trim = (v) => String(v ?? '').trim();

// 저장할 content: 앞뒤 공백을 지우고 이름·내용이 모두 빈 프로젝트와 수상은 뺀다.
// 화면이 다루지 않는 값(프로젝트 근거, 특화 이력서의 answers 등)은 그대로 둔다.
export function cleanContent(content) {
  const { basic, projects, awards, introduction } = content;
  return {
    ...content,
    ...(basic && { basic: Object.fromEntries(Object.entries(basic).map(([k, v]) => [k, typeof v === 'string' ? v.trim() : v])) }),
    ...(projects && { projects: projects.map((p) => ({ ...p, title: trim(p.title), desc: trim(p.desc) })).filter((p) => p.title || p.desc) }),
    ...(awards && { awards: awards.map((a) => ({ ...a, title: trim(a.title), date: trim(a.date) })).filter((a) => a.title || a.date) }),
    ...(introduction !== undefined && { introduction: trim(introduction) })
  };
}
