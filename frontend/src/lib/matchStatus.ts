import type { MatchCalculationStatus, MatchCalculationStatusType } from '../types/match';

export const MATCH_STATUS_LOCK_STALE_MINUTES = 5;

export function matchStatusQueryKey(userId: number | undefined) {
  return ['match-status', 'me', userId] as const;
}

export interface MatchStatusDisplay {
  enabled: boolean;
  message: string;
}

export function getMatchStatusDisplay(status: MatchCalculationStatus, now: Date): MatchStatusDisplay {
  if (status.status === 'FAILED') {
    return { enabled: true, message: '재계산에 실패했습니다.' };
  }

  if (status.status === 'IN_PROGRESS') {
    const isStale = isLockStale(status.updatedAt, now);
    return isStale
      ? { enabled: true, message: '재계산이 예상보다 오래 걸리고 있습니다. 재계산을 다시 시작해 주세요.' }
      : { enabled: false, message: '재계산 중입니다.' };
  }

  if (status.status === 'DONE') {
    return { enabled: false, message: '재계산이 완료되었습니다.' };
  }

  return { enabled: false, message: '아직 계산되지 않았습니다.' };
}

function isLockStale(updatedAt: string | null, now: Date): boolean {
  if (!updatedAt) return false;
  const elapsedMs = now.getTime() - new Date(updatedAt).getTime();
  return elapsedMs > MATCH_STATUS_LOCK_STALE_MINUTES * 60 * 1000;
}

export function didTransitionToDone(
  prevStatus: MatchCalculationStatusType | null | undefined,
  nextStatus: MatchCalculationStatusType | null
): boolean {
  return nextStatus === 'DONE' && (prevStatus === 'IN_PROGRESS' || prevStatus === 'FAILED');
}
