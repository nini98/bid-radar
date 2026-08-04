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
  // prevStatus가 undefined(이 관찰자의 최초 조회)인 경우도 true로 취급한다. 앱이 막 부팅된
  // 시점에 상태 조회와 bids 조회가 동시에 나가면, 재계산이 그 사이에 끝나 상태는 처음부터
  // DONE으로 응답하지만 bids는 계산 완료 전 값을 받았을 수 있다 — 이 관찰자가 "처음 봤다"는
  // 이유만으로 안전하게 넘기면 이 경쟁 상태에서 무효화가 누락된다. 호출자가 이 함수를 앱
  // 전역에 하나만 존재하는 관찰자(MatchStatusWatcher)에서만 쓰는 한, "최초 관찰=DONE"은
  // 화면 재방문마다 반복되지 않고 부팅 시 한 번만 발생하므로 과잉 무효화로 이어지지 않는다.
  return nextStatus === 'DONE' && prevStatus !== 'DONE';
}
