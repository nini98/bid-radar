import api from '../lib/axios';
import type { CompanyProfile, CompanyProfileRequest } from '../types/company';

export async function fetchCompanyProfile(): Promise<CompanyProfile | null> {
  return api.get('/companies/me');
}

export async function saveCompanyProfile(request: CompanyProfileRequest): Promise<CompanyProfile> {
  return api.put('/companies/me', request);
}
