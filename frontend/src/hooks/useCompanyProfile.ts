import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCompanyProfile, recalculateMatch, saveCompanyProfile } from '../api/company';
import type { CompanyProfileRequest } from '../types/company';
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
    mutationFn: async (request: CompanyProfileRequest) => {
      const profile = await saveCompanyProfile(request);
      let recalculated = true;
      try {
        await recalculateMatch();
      } catch {
        recalculated = false;
      }
      return { profile, recalculated };
    },
    onSuccess: ({ profile }) => {
      queryClient.setQueryData(['company', 'me', user?.id], profile);
      queryClient.invalidateQueries({ queryKey: ['bids'] });
    },
  });
}
