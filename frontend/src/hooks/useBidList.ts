import { useQuery } from '@tanstack/react-query';
import { fetchBidList } from '../api/bid';
import type { BidSearchParams } from '../types/bid';

export function useBidList(params: BidSearchParams) {
  return useQuery({
    queryKey: ['bids', params],
    queryFn: () => fetchBidList(params),
  });
}
