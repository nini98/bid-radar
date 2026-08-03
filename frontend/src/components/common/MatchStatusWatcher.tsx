import { useMatchStatus } from '../../hooks/useMatchStatus';

// 라우트 전환과 무관하게 항상 마운트되어 재계산 폴링과 DONE 전이 감지(→ bids 캐시 무효화)가
// 프로필 화면을 벗어나도 계속되도록 한다. 화면에는 아무것도 그리지 않는다.
export default function MatchStatusWatcher() {
  useMatchStatus();
  return null;
}
