import api from '../lib/axios';
import type { BidListResponse, BidNoticeDetail, BidSearchParams } from '../types/bid';

export async function fetchBidList(params: BidSearchParams): Promise<BidListResponse> {
  return api.get('/bids', { params });
}

export async function fetchBidDetail(bidId: number): Promise<BidNoticeDetail> {
  return api.get(`/bids/${bidId}`);
}
