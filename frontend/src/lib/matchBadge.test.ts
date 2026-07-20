import { describe, expect, it } from 'vitest';
import { matchBadgeStyle } from './matchBadge';
import type { MatchResultSummary } from '../types/bid';

function summary(totalScore: number): MatchResultSummary {
  return { totalScore, grade: 'RECOMMENDED', displayText: '추천' };
}

describe('matchBadgeStyle', () => {
  it('80점 이상이면 초록 스타일을 반환한다', () => {
    expect(matchBadgeStyle(summary(80))).toBe('bg-green-50 text-green-600');
  });

  it('60~79점이면 주황 스타일을 반환한다', () => {
    expect(matchBadgeStyle(summary(60))).toBe('bg-orange-50 text-orange-500');
    expect(matchBadgeStyle(summary(79))).toBe('bg-orange-50 text-orange-500');
  });

  it('60점 미만이면 회색 스타일을 반환한다', () => {
    expect(matchBadgeStyle(summary(59))).toBe('bg-gray-50 text-gray-400');
  });

  it('matchResult가 없으면 회색 스타일을 반환한다', () => {
    expect(matchBadgeStyle(null)).toBe('bg-gray-50 text-gray-400');
  });
});
