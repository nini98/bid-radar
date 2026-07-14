import { useQuery } from '@tanstack/react-query';
import { fetchBusinessAreas, fetchTechTags } from '../api/code';

export function useCodes() {
  const techTags = useQuery({ queryKey: ['codes', 'tech-tags'], queryFn: fetchTechTags });
  const businessAreas = useQuery({ queryKey: ['codes', 'business-areas'], queryFn: fetchBusinessAreas });

  return {
    techTags: techTags.data ?? [],
    businessAreas: businessAreas.data ?? [],
    isLoading: techTags.isLoading || businessAreas.isLoading,
    isError: techTags.isError || businessAreas.isError,
  };
}
