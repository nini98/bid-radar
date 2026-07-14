import type { CodeItem } from './code';

export interface ProjectExperience {
  projectType: string;
  description: string;
}

export interface BidPreference {
  preferredRegions: string[];
  budgetMin: number | null;
  budgetMax: number | null;
  deadlineMinDays: number | null;
  preferredBidTypes: string[];
  preferredContractTypes: string[];
}

export interface CompanyProfile {
  id: number;
  companyName: string;
  businessNumber: string | null;
  industry: string | null;
  foundedYear: number | null;
  companySize: string | null;
  region: string | null;
  address: string | null;
  website: string | null;
  strengths: string | null;
  techTags: CodeItem[];
  businessAreas: CodeItem[];
  certificates: string[];
  projectExperiences: ProjectExperience[];
  bidPreference: BidPreference | null;
  managerName: string | null;
  managerEmail: string | null;
  managerPhone: string | null;
  updatedAt: string;
}

export interface CompanyProfileRequest {
  companyName: string;
  businessNumber: string | null;
  industry: string | null;
  foundedYear: number | null;
  companySize: string | null;
  region: string | null;
  address: string | null;
  website: string | null;
  strengths: string | null;
  techTagIds: number[];
  businessAreaIds: number[];
  certificates: string[];
  projectExperiences: ProjectExperience[];
  bidPreference: BidPreference;
  managerName: string | null;
  managerEmail: string | null;
  managerPhone: string | null;
}
