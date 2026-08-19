import type { MatchResultSummary } from '../types/bid';

export function matchBadgeStyle(matchResult: MatchResultSummary | null): string {
  if (!matchResult) return 'bg-gray-50 text-gray-400';
  if (matchResult.status === 'FAILED') return 'bg-red-50 text-red-500';
  if (matchResult.totalScore !== null && matchResult.totalScore >= 80) return 'bg-green-50 text-green-600';
  if (matchResult.totalScore !== null && matchResult.totalScore >= 60) return 'bg-orange-50 text-orange-500';
  return 'bg-gray-50 text-gray-400';
}

export function matchBadgeLabel(matchResult: MatchResultSummary | null): string {
  if (!matchResult) return '미설정';
  if (matchResult.status === 'FAILED') return '계산 실패';
  return `${matchResult.displayText} ${matchResult.totalScore}점`;
}
