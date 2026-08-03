import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCompanyProfile, useSaveCompanyProfile } from '../hooks/useCompanyProfile';
import { useMatchStatus, useRetryMatchStatus } from '../hooks/useMatchStatus';
import { useCodes } from '../hooks/useCodes';
import TechTagSelect from '../components/company/TechTagSelect';
import BusinessAreaSelect from '../components/company/BusinessAreaSelect';
import CertificateList from '../components/company/CertificateList';
import ProjectExperienceList from '../components/company/ProjectExperienceList';
import BidPreferenceForm from '../components/company/BidPreferenceForm';
import MatchStatusButton from '../components/company/MatchStatusButton';
import Toast from '../components/common/Toast';
import { REGIONS } from '../constants/region';
import type { CodeItem } from '../types/code';
import type { BidPreference, CompanyProfile, CompanyProfileRequest, ProjectExperience } from '../types/company';

const EMPTY_BID_PREFERENCE: BidPreference = {
  preferredRegions: [],
  budgetMin: null,
  budgetMax: null,
  deadlineMinDays: null,
  preferredBidTypes: [],
  preferredContractTypes: [],
};

interface FormState {
  companyName: string;
  businessNumber: string;
  industry: string;
  foundedYear: string;
  companySize: string;
  region: string;
  address: string;
  website: string;
  strengths: string;
  managerName: string;
  managerEmail: string;
  managerPhone: string;
  techTagIds: number[];
  businessAreaIds: number[];
  certificates: string[];
  projectExperiences: ProjectExperience[];
  bidPreference: BidPreference;
}

function buildFormState(profile: CompanyProfile | null | undefined): FormState {
  if (!profile) {
    return {
      companyName: '',
      businessNumber: '',
      industry: '',
      foundedYear: '',
      companySize: '',
      region: '',
      address: '',
      website: '',
      strengths: '',
      managerName: '',
      managerEmail: '',
      managerPhone: '',
      techTagIds: [],
      businessAreaIds: [],
      certificates: [],
      projectExperiences: [],
      bidPreference: EMPTY_BID_PREFERENCE,
    };
  }
  return {
    companyName: profile.companyName,
    businessNumber: profile.businessNumber ?? '',
    industry: profile.industry ?? '',
    foundedYear: profile.foundedYear ? String(profile.foundedYear) : '',
    companySize: profile.companySize ?? '',
    region: profile.region ?? '',
    address: profile.address ?? '',
    website: profile.website ?? '',
    strengths: profile.strengths ?? '',
    managerName: profile.managerName ?? '',
    managerEmail: profile.managerEmail ?? '',
    managerPhone: profile.managerPhone ?? '',
    techTagIds: profile.techTags.map((t) => t.id),
    businessAreaIds: profile.businessAreas.map((b) => b.id),
    certificates: profile.certificates,
    projectExperiences: profile.projectExperiences,
    bidPreference: profile.bidPreference ?? EMPTY_BID_PREFERENCE,
  };
}

export default function CompanyProfilePage() {
  const navigate = useNavigate();
  const { data: profile, isLoading, isError } = useCompanyProfile();
  const { techTags, businessAreas, isLoading: isCodesLoading, isError: isCodesError } = useCodes();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 h-12 flex items-center justify-between">
          <span className="font-bold text-gray-900">회사 프로필</span>
          <button onClick={() => navigate('/')} className="text-sm text-gray-400 hover:text-gray-700">
            공고 목록으로
          </button>
        </div>
      </header>

      {(isLoading || isCodesLoading) && (
        <div className="max-w-2xl mx-auto px-4 py-16 text-center text-gray-400">불러오는 중...</div>
      )}

      {(isError || isCodesError) && (
        <div className="max-w-2xl mx-auto px-4 py-16 text-center text-gray-500">프로필을 불러오지 못했습니다.</div>
      )}

      {!isLoading && !isCodesLoading && !isError && !isCodesError && (
        <CompanyProfileForm profile={profile ?? null} techTags={techTags} businessAreas={businessAreas} />
      )}
    </div>
  );
}

interface CompanyProfileFormProps {
  profile: CompanyProfile | null;
  techTags: CodeItem[];
  businessAreas: CodeItem[];
}

function CompanyProfileForm({ profile, techTags, businessAreas }: CompanyProfileFormProps) {
  const { mutate: save, isPending } = useSaveCompanyProfile();
  const { data: matchStatus, isLoading: isMatchStatusLoading, isError: isMatchStatusError } = useMatchStatus();
  const { mutate: retry, isPending: isRetrying } = useRetryMatchStatus();
  const [form, setForm] = useState<FormState>(() => buildFormState(profile));
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const handleRetry = () => {
    retry(undefined, {
      onError: (err) => {
        setToast({ message: err instanceof Error ? err.message : '재계산 요청에 실패했습니다.', type: 'error' });
      },
    });
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!form.companyName.trim()) {
      setToast({ message: '회사명을 입력해주세요.', type: 'error' });
      return;
    }

    const request: CompanyProfileRequest = {
      companyName: form.companyName,
      businessNumber: form.businessNumber || null,
      industry: form.industry || null,
      foundedYear: form.foundedYear ? Number(form.foundedYear) : null,
      companySize: form.companySize || null,
      region: form.region || null,
      address: form.address || null,
      website: form.website || null,
      strengths: form.strengths || null,
      techTagIds: form.techTagIds,
      businessAreaIds: form.businessAreaIds,
      certificates: form.certificates,
      projectExperiences: form.projectExperiences,
      bidPreference: form.bidPreference,
      managerName: form.managerName || null,
      managerEmail: form.managerEmail || null,
      managerPhone: form.managerPhone || null,
    };

    save(request, {
      onSuccess: () => {
        setToast({ message: '저장되었습니다.', type: 'success' });
      },
      onError: (err) => {
        setToast({ message: err instanceof Error ? err.message : '저장에 실패했습니다.', type: 'error' });
      },
    });
  };

  return (
    <>
      <form onSubmit={handleSubmit} className="max-w-2xl mx-auto px-4 py-6 space-y-6">
        {profile && (
          <MatchStatusButton
            status={matchStatus}
            isLoading={isMatchStatusLoading}
            isError={isMatchStatusError}
            isRetrying={isRetrying}
            onRetry={handleRetry}
          />
        )}

        <section className="bg-white border border-gray-100 rounded-lg p-4 space-y-3">
          <h2 className="text-sm font-semibold text-gray-900">기본 정보</h2>
          <div className="grid grid-cols-2 gap-3">
            <Field label="회사명" required value={form.companyName} onChange={(v) => setForm({ ...form, companyName: v })} />
            <Field label="사업자등록번호" value={form.businessNumber} onChange={(v) => setForm({ ...form, businessNumber: v })} />
            <Field label="업종" value={form.industry} onChange={(v) => setForm({ ...form, industry: v })} />
            <Field label="설립연도" type="number" value={form.foundedYear} onChange={(v) => setForm({ ...form, foundedYear: v })} />
            <Field label="기업 규모" value={form.companySize} onChange={(v) => setForm({ ...form, companySize: v })} />
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">지역</label>
              <select
                value={form.region}
                onChange={(e) => setForm({ ...form, region: e.target.value })}
                className="w-full text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
              >
                <option value="">선택 안 함</option>
                {REGIONS.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>
            <Field label="주소" value={form.address} onChange={(v) => setForm({ ...form, address: v })} />
            <Field label="웹사이트" value={form.website} onChange={(v) => setForm({ ...form, website: v })} />
          </div>
          <Field label="강점" textarea value={form.strengths} onChange={(v) => setForm({ ...form, strengths: v })} />
        </section>

        <section className="bg-white border border-gray-100 rounded-lg p-4 space-y-3">
          <h2 className="text-sm font-semibold text-gray-900">담당자 정보</h2>
          <div className="grid grid-cols-3 gap-3">
            <Field label="담당자명" value={form.managerName} onChange={(v) => setForm({ ...form, managerName: v })} />
            <Field label="이메일" type="email" value={form.managerEmail} onChange={(v) => setForm({ ...form, managerEmail: v })} />
            <Field label="연락처" value={form.managerPhone} onChange={(v) => setForm({ ...form, managerPhone: v })} />
          </div>
        </section>

        <section className="bg-white border border-gray-100 rounded-lg p-4 space-y-4">
          <h2 className="text-sm font-semibold text-gray-900">기술 태그 / 사업 분야</h2>
          <TechTagSelect options={techTags} selectedIds={form.techTagIds} onChange={(ids) => setForm({ ...form, techTagIds: ids })} />
          <BusinessAreaSelect
            options={businessAreas}
            selectedIds={form.businessAreaIds}
            onChange={(ids) => setForm({ ...form, businessAreaIds: ids })}
          />
        </section>

        <section className="bg-white border border-gray-100 rounded-lg p-4">
          <CertificateList items={form.certificates} onChange={(next) => setForm({ ...form, certificates: next })} />
        </section>

        <section className="bg-white border border-gray-100 rounded-lg p-4">
          <ProjectExperienceList
            items={form.projectExperiences}
            onChange={(next) => setForm({ ...form, projectExperiences: next })}
          />
        </section>

        <section className="bg-white border border-gray-100 rounded-lg p-4">
          <h2 className="text-sm font-semibold text-gray-900 mb-3">입찰 선호 조건</h2>
          <BidPreferenceForm value={form.bidPreference} onChange={(next) => setForm({ ...form, bidPreference: next })} />
        </section>

        <button
          type="submit"
          disabled={isPending}
          className="w-full py-2.5 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {isPending ? '저장 중...' : '저장'}
        </button>
      </form>

      {toast && <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />}
    </>
  );
}

interface FieldProps {
  label: string;
  value: string;
  onChange: (v: string) => void;
  type?: string;
  required?: boolean;
  textarea?: boolean;
}

function Field({ label, value, onChange, type = 'text', required, textarea }: FieldProps) {
  return (
    <div>
      <label className="text-sm font-medium text-gray-700 mb-1 block">
        {label}
        {required && <span className="text-red-500"> *</span>}
      </label>
      {textarea ? (
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          rows={3}
          className="w-full text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400 scroll-mt-16"
        />
      ) : (
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          required={required}
          className="w-full text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400 scroll-mt-16"
        />
      )}
    </div>
  );
}
