import { Link } from 'react-router-dom';
import type { MatchResult } from '../../types/bid';
import MatchBadge from './MatchBadge';

interface Props {
  matchResult: MatchResult | null;
}

const BREAKDOWN_LABELS: { key: keyof MatchResult; label: string }[] = [
  { key: 'scoreTech', label: '기술 적합도' },
  { key: 'scoreBusiness', label: '사업 분야' },
  { key: 'scoreBudget', label: '예산 범위' },
  { key: 'scoreRegion', label: '지역' },
];

export default function BidMatchSection({ matchResult }: Props) {
  if (!matchResult) {
    return (
      <section className="bg-white rounded-lg border border-gray-100 p-5">
        <h2 className="text-sm font-semibold text-gray-900 mb-3">적합도</h2>
        <p className="text-sm text-gray-500">
          회사 프로필을 설정하면 이 공고의 적합도를 확인할 수 있습니다.{' '}
          <Link to="/company/profile" className="text-blue-600 hover:underline">
            프로필 설정하기
          </Link>
        </p>
      </section>
    );
  }

  return (
    <section className="bg-white rounded-lg border border-gray-100 p-5">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-sm font-semibold text-gray-900">적합도</h2>
        <MatchBadge matchResult={matchResult} />
      </div>

      <ul className="grid grid-cols-2 gap-y-2 text-sm mb-3">
        {BREAKDOWN_LABELS.map(({ key, label }) => {
          const value = matchResult[key];
          return (
            <li key={key} className="flex justify-between pr-4">
              <span className="text-gray-400">{label}</span>
              <span className="text-gray-800">{typeof value === 'number' ? `${value}점` : '-'}</span>
            </li>
          );
        })}
      </ul>

      {matchResult.scoreReason && (
        <div className="pt-3 border-t border-gray-100">
          <p className="text-sm text-gray-700 whitespace-pre-line">{matchResult.scoreReason}</p>
        </div>
      )}
    </section>
  );
}
