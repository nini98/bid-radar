import api from '../lib/axios';
import type { MatchCalculationStatus } from '../types/match';

export async function fetchMatchStatus(): Promise<MatchCalculationStatus> {
  return api.get('/companies/me/match-status');
}

export async function retryMatchStatus(): Promise<void> {
  return api.post('/companies/me/match-status/retry');
}
