import { describe, expect, it } from 'vitest';
import { matchBadgeLabel, matchBadgeStyle } from './matchBadge';
import type { MatchResultSummary } from '../types/bid';

function summary(totalScore: number): MatchResultSummary {
  return { status: 'SUCCESS', totalScore, grade: 'RECOMMENDED', displayText: '추천' };
}

function failedSummary(): MatchResultSummary {
  return { status: 'FAILED', totalScore: null, grade: null, displayText: null };
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

  it('status가 FAILED면 빨강 스타일을 반환한다', () => {
    expect(matchBadgeStyle(failedSummary())).toBe('bg-red-50 text-red-500');
  });
});

describe('matchBadgeLabel', () => {
  it('matchResult가 없으면 "미설정"을 반환한다', () => {
    expect(matchBadgeLabel(null)).toBe('미설정');
  });

  it('status가 SUCCESS면 등급과 점수를 반환한다', () => {
    expect(matchBadgeLabel(summary(85))).toBe('추천 85점');
  });

  it('status가 FAILED면 "계산 실패"를 반환한다', () => {
    expect(matchBadgeLabel(failedSummary())).toBe('계산 실패');
  });
});
