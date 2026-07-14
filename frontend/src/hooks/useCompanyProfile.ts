import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCompanyProfile, recalculateMatch, saveCompanyProfile } from '../api/company';
import type { CompanyProfileRequest } from '../types/company';

export function useCompanyProfile() {
  return useQuery({
    queryKey: ['company', 'me'],
    queryFn: fetchCompanyProfile,
  });
}

export function useSaveCompanyProfile() {
  const queryClient = useQueryClient();

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
      queryClient.setQueryData(['company', 'me'], profile);
    },
  });
}
