import { useRef } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchMatchStatus, retryMatchStatus } from '../api/match';
import { matchStatusQueryKey } from '../lib/matchStatus';
import { useCompanyProfile } from './useCompanyProfile';
import { useMe } from './useAuth';

const POLL_INTERVAL_MS = 5000;

// DONE 전이 감지와 그에 따른 bids 캐시 무효화는 이 훅이 아니라 MatchStatusWatcher(앱 루트에 딱
// 한 번만 마운트)가 전담한다. 이 훅은 CompanyProfilePage처럼 화면마다 마운트/언마운트되는
// 소비자도 있는데, 그런 곳에서 각자 전이를 추적하면 페이지를 다시 방문할 때마다 "최초 관찰"이
// 반복되어 중복/과잉 무효화가 생긴다.
export function useMatchStatus() {
  const { data: user } = useMe();
  const { data: profile } = useCompanyProfile();

  return useQuery({
    queryKey: matchStatusQueryKey(user?.id),
    queryFn: fetchMatchStatus,
    enabled: !!user && !!profile,
    refetchInterval: (q) => (q.state.data?.status === 'IN_PROGRESS' ? POLL_INTERVAL_MS : false),
  });
}

export function useRetryMatchStatus() {
  const { data: user } = useMe();
  const queryClient = useQueryClient();
  // isPending은 React state라 같은 tick에서 연달아 들어오는 클릭(더블클릭 등)에는
  // 리렌더가 끼어들 시간이 없어 두 번째 클릭도 여전히 false로 읽을 수 있다. ref는
  // 리렌더 없이 즉시 갱신되므로 그 좁은 창도 확실히 막는다.
  const isRetryingRef = useRef(false);

  const mutation = useMutation({
    mutationFn: retryMatchStatus,
    onSuccess: () => {
      // 즉시 캐시를 잠가 실제 상태를 다시 받아오기 전까지 버튼이 활성 상태로 남지 않게 하고,
      // 무효화 Promise를 반환해 그 재조회가 끝날 때까지 mutation을 pending으로 유지한다
      // (그래야 재조회가 끝나기 전 중복 클릭이 이미 성공한 요청에 대해 서버 409로 거부되지 않는다).
      queryClient.setQueryData(matchStatusQueryKey(user?.id), {
        status: 'IN_PROGRESS',
        updatedAt: new Date().toISOString(),
      });
      return queryClient.invalidateQueries({ queryKey: matchStatusQueryKey(user?.id) });
    },
    onSettled: () => {
      isRetryingRef.current = false;
    },
  });

  const retry: typeof mutation.mutate = (variables, options) => {
    if (isRetryingRef.current) return;
    isRetryingRef.current = true;
    mutation.mutate(variables, options);
  };

  return { ...mutation, mutate: retry };
}
