export type BidStatus = 'OPEN' | 'CLOSED' | 'CANCELED';
export type BidSortType = 'LATEST' | 'DEADLINE' | 'SCORE';
export type MatchGrade = 'STRONG_REVIEW' | 'RECOMMENDED' | 'NEED_REVIEW';

export interface MatchResultSummary {
  totalScore: number;
  grade: MatchGrade;
  displayText: string;
}

export interface MatchResult extends MatchResultSummary {
  scoreTech: number | null;
  scoreBusiness: number | null;
  scoreBudget: number | null;
  scoreRegion: number | null;
  matchedKeywords: string | null;
  scoreReason: string | null;
}

export interface BidNoticeSummary {
  id: number;
  title: string;
  agency: string | null;
  budget: number | null;
  region: string | null;
  bidType: string | null;
  status: BidStatus;
  bidDeadline: string | null;
  publishedAt: string | null;
  matchResult: MatchResultSummary | null;
}

export interface BidAttachment {
  id: number;
  fileName: string;
  fileType: string | null;
  fileSize: number | null;
  downloadUrl: string | null;
  isDownloaded: boolean;
  isAnalyzed: boolean;
}

export interface BidNoticeDetail {
  id: number;
  externalNoticeId: string;
  title: string;
  agency: string | null;
  budget: number | null;
  region: string | null;
  bidType: string | null;
  contractType: string | null;
  industryRestriction: string | null;
  regionRestriction: string | null;
  qualificationSummary: string | null;
  noticeUrl: string | null;
  publishedAt: string | null;
  questionDeadline: string | null;
  documentDeadline: string | null;
  bidDeadline: string | null;
  openAt: string | null;
  status: BidStatus;
  collectedAt: string | null;
  attachments: BidAttachment[];
  matchResult: MatchResult | null;
}

export interface BidListSummary {
  todayNewCount: number;
  todayRecommendedCount: number;
  todayDeadlineCount: number;
}

export interface PageMeta {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface BidListResponse {
  summary: BidListSummary;
  content: BidNoticeSummary[];
  page: PageMeta;
}

export interface BidSearchParams {
  keyword?: string;
  region?: string;
  budgetMin?: number;
  budgetMax?: number;
  deadlineDays?: number;
  grade?: MatchGrade;
  sort?: BidSortType;
  page?: number;
  size?: number;
}
