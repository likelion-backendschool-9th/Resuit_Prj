import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import FollowButton from '../common/FollowButton.jsx';

// 공고 상세 윗부분 (회사 · 제목 · 소개 · 자격 요건 · 우대 사항). 일반 · 특화 공고가 함께 쓴다.
export default function JobHead({ job }) {
  const special = job.type === 'special';
  return (
    <div className="section" style={{ gap: 20, padding: 24 }}>
      <div className="row" style={{ gap: 12 }}>
        <Avatar initial={job.initial} color={job.av} size={56} company />
        <div className="grow"><div className="bold" style={{ fontSize: 16 }}>{job.company}</div><div className="muted" style={{ fontSize: 14 }}>@{job.handle} · 팔로워 [N]명</div></div>
        <FollowButton handle={job.handle} offVariant="outline" className="job-detail__follow" />
      </div>
      <div className="stack" style={{ gap: 10 }}>
        <div className="row-wrap" style={{ gap: 6 }}>
          <Badge tone={special ? 'special' : 'general'} size="md">{special ? '특화 공고' : '일반 공고'}</Badge>
          <Badge size="md">마감 {job.dday}</Badge>
          <Badge size="md">{job.employment}</Badge>
        </div>
        <h2 className="job-detail__title">{job.title}</h2>
        <div className="muted" style={{ fontSize: 14 }}>{job.detailMeta}</div>
      </div>
      <p style={{ lineHeight: 1.75 }}>{job.intro}</p>
      <div className="grid-2" style={{ gap: 16 }}>
        <section className="job-detail__box"><h3>자격 요건</h3><ul>{job.requirements.map((r) => <li key={r}>{r}</li>)}</ul></section>
        <section className="job-detail__box"><h3>우대 사항</h3><ul>{job.preferred.map((r) => <li key={r}>{r}</li>)}</ul></section>
      </div>
    </div>
  );
}
