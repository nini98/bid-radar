import { useQuery } from '@tanstack/react-query';
import { fetchBidDetail } from '../api/bid';

export function useBidDetail(bidId: number) {
  return useQuery({
    queryKey: ['bids', bidId],
    queryFn: () => fetchBidDetail(bidId),
  });
}
