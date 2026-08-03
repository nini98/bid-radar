export type MatchCalculationStatusType = 'IN_PROGRESS' | 'DONE' | 'FAILED';

export interface MatchCalculationStatus {
  status: MatchCalculationStatusType | null;
  updatedAt: string | null;
}
