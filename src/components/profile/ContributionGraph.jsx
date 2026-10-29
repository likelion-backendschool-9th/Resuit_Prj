import { useMemo } from 'react';
import './ContributionGraph.css';

// GitHub 잔디. 실제로는 GraphQL contributionsCollection 의 주별 기여 수를 받아 레벨(0~4)로 바꿔 넣는다.
// weeks: number[][] (52주 × 7일, 값은 0~4). 안 넘기면 예시 데이터를 만든다.
function sampleWeeks() {
  let seed = 7;
  const rand = () => { seed = (seed * 9301 + 49297) % 233280; return seed / 233280; };
  return Array.from({ length: 52 }, (_, w) =>
    Array.from({ length: 7 }, (_, d) => {
      const r = rand() + (w > 30 ? 0.15 : 0) - (d === 0 || d === 6 ? 0.25 : 0);
      return r < 0.35 ? 0 : r < 0.6 ? 1 : r < 0.8 ? 2 : r < 0.93 ? 3 : 4;
    })
  );
}

export default function ContributionGraph({ weeks }) {
  const data = useMemo(() => weeks || sampleWeeks(), [weeks]);
  return (
    <div className="grass">
      <div className="grass__scroll">
        <div className="grass__grid" role="img" aria-label="최근 1년 GitHub 기여 기록">
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
