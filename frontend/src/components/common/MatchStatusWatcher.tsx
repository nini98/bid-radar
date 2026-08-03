import { useEffect, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useMatchStatus } from '../../hooks/useMatchStatus';
import { didTransitionToDone } from '../../lib/matchStatus';
import type { MatchCalculationStatusType } from '../../types/match';

// 라우트 전환과 무관하게 항상 마운트되어 재계산 폴링과 DONE 전이 감지(→ bids 캐시 무효화)가
// 프로필 화면을 벗어나도 계속되도록 한다. 화면에는 아무것도 그리지 않는다.
// 앱 전체에서 이 컴포넌트 하나만 전이 추적/무효화를 담당한다 — CompanyProfilePage 등 다른
// 소비자는 useMatchStatus()를 표시용으로만 쓴다 (자세한 이유는 useMatchStatus.ts 주석 참고).
export default function MatchStatusWatcher() {
  const { data } = useMatchStatus();
  const queryClient = useQueryClient();
  const prevStatusRef = useRef<MatchCalculationStatusType | null | undefined>(undefined);

  useEffect(() => {
    if (!data) return;
    const nextStatus = data.status;
    if (didTransitionToDone(prevStatusRef.current, nextStatus)) {
      queryClient.invalidateQueries({ queryKey: ['bids'] });
    }
    prevStatusRef.current = nextStatus;
  }, [data, queryClient]);

  return null;
}
