import { useState } from 'react';
import type { BidSortType, MatchGrade } from '../../types/bid';
import { REGIONS } from '../../constants/region';

interface Props {
  keyword: string;
  sort: BidSortType;
  region: string;
  grade: MatchGrade | '';
  onKeywordChange: (v: string) => void;
  onSortChange: (v: BidSortType) => void;
  onRegionChange: (v: string) => void;
  onGradeChange: (v: MatchGrade | '') => void;
  onSearch: () => void;
}

const SORT_OPTIONS: { label: string; value: BidSortType }[] = [
  { label: '최신순', value: 'LATEST' },
  { label: '마감일순', value: 'DEADLINE' },
  { label: '적합도순', value: 'SCORE' },
];

const GRADE_OPTIONS: { label: string; value: MatchGrade | '' }[] = [
  { label: '전체', value: '' },
  { label: '적극 검토', value: 'STRONG_REVIEW' },
  { label: '추천', value: 'RECOMMENDED' },
  { label: '검토 필요', value: 'NEED_REVIEW' },
];

export default function BidSearchBar({ keyword, sort, region, grade, onKeywordChange, onSortChange, onRegionChange, onGradeChange, onSearch }: Props) {
  const [inputValue, setInputValue] = useState(keyword);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      onKeywordChange(inputValue);
      onSearch();
    }
  };

  return (
    <div className="bg-white border border-gray-100 rounded-lg p-4 mb-4">
      <div className="flex gap-2 mb-3">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="공고명 또는 기관명 검색"
          className="flex-1 text-sm border border-gray-200 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-400"
        />
        <button
          onClick={() => { onKeywordChange(inputValue); onSearch(); }}
          className="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          검색
        </button>
      </div>

      <div className="flex gap-3">
        <select
          value={region}
          onChange={(e) => onRegionChange(e.target.value)}
          className="text-sm border border-gray-200 rounded px-2 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
        >
          <option value="">전체 지역</option>
          {REGIONS.map((r) => (
            <option key={r} value={r}>{r}</option>
          ))}
        </select>

        <select
          value={grade}
          onChange={(e) => onGradeChange(e.target.value as MatchGrade | '')}
          className="text-sm border border-gray-200 rounded px-2 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
        >
          {GRADE_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>

        <div className="flex gap-1 ml-auto">
          {SORT_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              onClick={() => onSortChange(opt.value)}
              className={`px-3 py-1.5 text-xs rounded border ${
                sort === opt.value
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'border-gray-200 text-gray-600 hover:bg-gray-50'
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
