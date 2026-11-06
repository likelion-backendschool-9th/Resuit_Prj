import './ContributionGraph.css';

// GitHub 잔디. weeks: number[][] (주 × 7일, 값은 0~4).
export default function ContributionGraph({ weeks, label = 'GitHub 기여 기록' }) {
  const data = weeks?.length ? weeks : [];

  if (!data.length) {
    return (
      <div className="grass">
        <div className="empty" style={{ padding: '24px 0' }}>기여 기록이 없어요.</div>
      </div>
    );
  }

  return (
    <div className="grass">
      <div className="grass__scroll">
        <div className="grass__grid" role="img" aria-label={label}>
          {data.map((week, w) => (
            <div key={w} className="grass__week">
              {week.map((lvl, d) => <span key={d} className={`grass__cell g${lvl}`} />)}
            </div>
          ))}
        </div>
      </div>
      <div className="grass__legend">
        적음
        {[0, 1, 2, 3, 4].map((l) => <span key={l} className={`grass__cell g${l}`} />)}
        많음
      </div>
    </div>
  );
}
