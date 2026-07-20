import { describe, expect, it } from 'vitest';
import { formatBudget, formatDate, formatDateTime } from './format';

describe('formatBudget', () => {
  it('1억 이상이면 억 단위로 표시한다', () => {
    expect(formatBudget(150_000_000)).toBe('1.5억');
  });

  it('1만 이상 1억 미만이면 만 단위로 표시한다', () => {
    expect(formatBudget(50_000)).toBe('5만');
  });

  it('1만 미만이면 원 단위 그대로 표시한다', () => {
    expect(formatBudget(5000)).toBe('5,000원');
  });
});

describe('formatDate', () => {
  it('ISO 문자열에서 날짜 부분만 추출한다', () => {
    expect(formatDate('2026-07-20T10:30:00')).toBe('2026-07-20');
  });
});

describe('formatDateTime', () => {
  it('ISO 문자열에서 날짜와 시:분을 추출한다', () => {
    expect(formatDateTime('2026-07-20T10:30:00')).toBe('2026-07-20 10:30');
  });
});
