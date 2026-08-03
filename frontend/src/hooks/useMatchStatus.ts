import { useEffect, useRef } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchMatchStatus, retryMatchStatus } from '../api/match';
import { didTransitionToDone, matchStatusQueryKey } from '../lib/matchStatus';
import type { MatchCalculationStatusType } from '../types/match';
import { useCompanyProfile } from './useCompanyProfile';
import { useMe } from './useAuth';

const POLL_INTERVAL_MS = 5000;

export function useMatchStatus() {
  const { data: user } = useMe();
  const { data: profile } = useCompanyProfile();
  const queryClient = useQueryClient();
  const prevStatusRef = useRef<MatchCalculationStatusType | null | undefined>(undefined);

  const query = useQuery({
    queryKey: matchStatusQueryKey(user?.id),
    queryFn: fetchMatchStatus,
    enabled: !!user && !!profile,
    refetchInterval: (q) => (q.state.data?.status === 'IN_PROGRESS' ? POLL_INTERVAL_MS : false),
  });

  useEffect(() => {
    if (!query.data) return;
    const nextStatus = query.data.status;
    if (didTransitionToDone(prevStatusRef.current, nextStatus)) {
      queryClient.invalidateQueries({ queryKey: ['bids'] });
    }
    prevStatusRef.current = nextStatus;
  }, [query.data, queryClient]);

  return query;
}

export function useRetryMatchStatus() {
  const { data: user } = useMe();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: retryMatchStatus,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: matchStatusQueryKey(user?.id) });
    },
  });
}
