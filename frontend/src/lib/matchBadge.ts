import type { MatchResultSummary } from '../types/bid';

export function matchBadgeStyle(matchResult: MatchResultSummary | null): string {
  if (!matchResult) return 'bg-gray-50 text-gray-400';
  if (matchResult.totalScore >= 80) return 'bg-green-50 text-green-600';
  if (matchResult.totalScore >= 60) return 'bg-orange-50 text-orange-500';
  return 'bg-gray-50 text-gray-400';
}
