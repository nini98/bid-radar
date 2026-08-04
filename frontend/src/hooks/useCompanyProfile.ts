import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCompanyProfile, saveCompanyProfile } from '../api/company';
import { matchStatusQueryKey } from '../lib/matchStatus';
import { useMe } from './useAuth';

export function useCompanyProfile() {
  const { data: user } = useMe();

  return useQuery({
    queryKey: ['company', 'me', user?.id],
    queryFn: fetchCompanyProfile,
    enabled: !!user,
  });
}

export function useSaveCompanyProfile() {
  const queryClient = useQueryClient();
  const { data: user } = useMe();

  return useMutation({
    mutationFn: saveCompanyProfile,
    onSuccess: (profile) => {
      queryClient.setQueryData(['company', 'me', user?.id], profile);
      // 저장 성공은 백엔드가 재계산을 새로 트리거했다는 뜻이므로, 캐시된 이전 상태(예: DONE)가
      // 새 폴링을 막지 않도록 IN_PROGRESS로 먼저 반영하고 실제 상태를 다시 조회한다.
      queryClient.setQueryData(matchStatusQueryKey(user?.id), {
        status: 'IN_PROGRESS',
        updatedAt: new Date().toISOString(),
      });
      queryClient.invalidateQueries({ queryKey: matchStatusQueryKey(user?.id) });
    },
  });
}
