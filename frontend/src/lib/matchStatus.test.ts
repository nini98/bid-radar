import { describe, expect, it } from 'vitest';
import { didTransitionToDone, getMatchStatusDisplay } from './matchStatus';

const NOW = new Date('2026-08-03T12:00:00');

describe('getMatchStatusDisplay', () => {
  it('FAILED이면 활성화되고 실패 문구를 보여준다', () => {
    const result = getMatchStatusDisplay({ status: 'FAILED', updatedAt: '2026-08-03T11:00:00' }, NOW);
    expect(result).toEqual({ enabled: true, message: '재계산에 실패했습니다.' });
  });

  it('IN_PROGRESS이고 updatedAt이 5분 이내면 비활성화되고 진행 중 문구를 보여준다', () => {
    const result = getMatchStatusDisplay({ status: 'IN_PROGRESS', updatedAt: '2026-08-03T11:56:00' }, NOW);
    expect(result).toEqual({ enabled: false, message: '재계산 중입니다.' });
  });

  it('IN_PROGRESS이고 updatedAt이 정확히 5분 전이면 아직 신선한 것으로 본다 (경계값)', () => {
    const result = getMatchStatusDisplay({ status: 'IN_PROGRESS', updatedAt: '2026-08-03T11:55:00' }, NOW);
    expect(result).toEqual({ enabled: false, message: '재계산 중입니다.' });
  });

  it('IN_PROGRESS이고 updatedAt이 5분을 초과해 지나면 활성화되고 지연 문구를 보여준다', () => {
    const result = getMatchStatusDisplay({ status: 'IN_PROGRESS', updatedAt: '2026-08-03T11:54:59' }, NOW);
    expect(result).toEqual({
      enabled: true,
      message: '재계산이 예상보다 오래 걸리고 있습니다. 재계산을 다시 시작해 주세요.',
    });
  });

  it('DONE이면 비활성화되고 완료 문구를 보여준다', () => {
    const result = getMatchStatusDisplay({ status: 'DONE', updatedAt: '2026-08-03T11:00:00' }, NOW);
    expect(result).toEqual({ enabled: false, message: '재계산이 완료되었습니다.' });
  });

  it('이력이 없으면(status: null) 비활성화되고 미계산 문구를 보여준다', () => {
    const result = getMatchStatusDisplay({ status: null, updatedAt: null }, NOW);
    expect(result).toEqual({ enabled: false, message: '아직 계산되지 않았습니다.' });
  });

  // 백엔드가 감사 타임스탬프를 오프셋 포함(UTC, `Z`) 문자열로 응답하도록 바뀐 뒤에도
  // 동일한 판정 로직이 그대로 성립하는지 확인한다 (Issue #47).
  describe('updatedAt이 오프셋 포함(UTC) 문자열이어도 동일하게 동작한다', () => {
    const NOW_UTC = new Date('2026-08-03T12:00:00Z');

    it('IN_PROGRESS이고 5분 이내면 비활성화된다', () => {
      const result = getMatchStatusDisplay({ status: 'IN_PROGRESS', updatedAt: '2026-08-03T11:56:00Z' }, NOW_UTC);
      expect(result).toEqual({ enabled: false, message: '재계산 중입니다.' });
    });

    it('IN_PROGRESS이고 5분을 초과하면 활성화된다', () => {
      const result = getMatchStatusDisplay({ status: 'IN_PROGRESS', updatedAt: '2026-08-03T11:54:59Z' }, NOW_UTC);
      expect(result).toEqual({
        enabled: true,
        message: '재계산이 예상보다 오래 걸리고 있습니다. 재계산을 다시 시작해 주세요.',
      });
    });
  });
});

describe('didTransitionToDone', () => {
  it('IN_PROGRESS에서 DONE으로 바뀌면 true를 반환한다', () => {
    expect(didTransitionToDone('IN_PROGRESS', 'DONE')).toBe(true);
  });

  it('FAILED에서 DONE으로 바뀌면 true를 반환한다', () => {
    expect(didTransitionToDone('FAILED', 'DONE')).toBe(true);
  });

  it('이미 DONE이었다가 다시 DONE이면 false를 반환한다 (전이 아님)', () => {
    expect(didTransitionToDone('DONE', 'DONE')).toBe(false);
  });

  it('관찰자의 최초 조회(undefined)에서 DONE이면 true를 반환한다 (부팅 시 조회 경쟁 대비)', () => {
    expect(didTransitionToDone(undefined, 'DONE')).toBe(true);
  });

  it('이력 없음(null)에서 DONE으로 바뀌면 true를 반환한다', () => {
    expect(didTransitionToDone(null, 'DONE')).toBe(true);
  });

  it('IN_PROGRESS에서 FAILED로 바뀌면 false를 반환한다', () => {
    expect(didTransitionToDone('IN_PROGRESS', 'FAILED')).toBe(false);
  });
});
