import type { BidPreference } from '../../types/company';
import { REGIONS } from '../../constants/region';
import { BID_TYPES, CONTRACT_TYPES } from '../../constants/bidPreference';
import MultiSelect from './MultiSelect';

interface Props {
  value: BidPreference;
  onChange: (next: BidPreference) => void;
}

export default function BidPreferenceForm({ value, onChange }: Props) {
  return (
    <div className="space-y-4">
      <MultiSelect
        label="선호 지역"
        options={REGIONS.map((r) => ({ value: r, label: r }))}
        selected={value.preferredRegions}
        onChange={(next) => onChange({ ...value, preferredRegions: next })}
      />

      <div className="flex gap-3">
        <div className="flex-1">
          <label className="text-sm font-medium text-gray-700 mb-1 block">예산 하한 (원)</label>
          <input
            type="number"
            value={value.budgetMin ?? ''}
            onChange={(e) => onChange({ ...value, budgetMin: e.target.value ? Number(e.target.value) : null })}
            className="w-full text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
          />
        </div>
        <div className="flex-1">
          <label className="text-sm font-medium text-gray-700 mb-1 block">예산 상한 (원)</label>
          <input
            type="number"
            value={value.budgetMax ?? ''}
            onChange={(e) => onChange({ ...value, budgetMax: e.target.value ? Number(e.target.value) : null })}
            className="w-full text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
          />
        </div>
        <div className="flex-1">
          <label className="text-sm font-medium text-gray-700 mb-1 block">마감 여유일 (일)</label>
          <input
            type="number"
            value={value.deadlineMinDays ?? ''}
            onChange={(e) => onChange({ ...value, deadlineMinDays: e.target.value ? Number(e.target.value) : null })}
            className="w-full text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
          />
        </div>
      </div>

      <MultiSelect
        label="선호 입찰방식"
        options={BID_TYPES.map((t) => ({ value: t, label: t }))}
        selected={value.preferredBidTypes}
        onChange={(next) => onChange({ ...value, preferredBidTypes: next })}
      />

      <MultiSelect
        label="선호 계약방식"
        options={CONTRACT_TYPES.map((t) => ({ value: t, label: t }))}
        selected={value.preferredContractTypes}
        onChange={(next) => onChange({ ...value, preferredContractTypes: next })}
      />
    </div>
  );
}
