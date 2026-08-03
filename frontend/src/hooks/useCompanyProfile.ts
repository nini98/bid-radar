import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCompanyProfile, saveCompanyProfile } from '../api/company';
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
    },
  });
}
