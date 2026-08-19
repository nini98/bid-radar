import type { MatchResultSummary } from '../../types/bid';
import { matchBadgeLabel, matchBadgeStyle } from '../../lib/matchBadge';

interface Props {
  matchResult: MatchResultSummary | null;
}

export default function MatchBadge({ matchResult }: Props) {
  const label = matchBadgeLabel(matchResult);

  return (
    <span className={`shrink-0 text-xs font-semibold px-2 py-0.5 rounded ${matchBadgeStyle(matchResult)}`}>
      {label}
    </span>
  );
}
