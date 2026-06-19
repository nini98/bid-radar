import type { BidNoticeSummary } from '../../types/bid';

interface Props {
  bid: BidNoticeSummary;
}

export default function BidCard({ bid }: Props) {
  const dDay = getDDay(bid.bidDeadline);

  return (
    <div className="bg-white rounded-lg border border-gray-100 p-4 hover:shadow-sm transition-shadow">
      <div className="flex justify-between items-start gap-3 mb-2">
        <h3 className="text-sm font-medium text-gray-900 leading-snug line-clamp-2 flex-1">
          {bid.title}
        </h3>
        <span className={`shrink-0 text-xs font-semibold px-2 py-0.5 rounded ${dDayStyle(dDay)}`}>
          {dDay !== null ? (dDay === 0 ? 'D-Day' : dDay < 0 ? `D${dDay}` : `D+${dDay}`) : '-'}
        </span>
      </div>

      <p className="text-xs text-gray-500 mb-3">{bid.agency ?? '기관 미상'}</p>

      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
        {bid.budget !== null && (
          <span>💰 {formatBudget(bid.budget)}</span>
        )}
        {bid.region && <span>📍 {bid.region}</span>}
        {bid.bidType && <span>📋 {bid.bidType}</span>}
        {bid.bidDeadline && (
          <span>⏰ 마감 {formatDate(bid.bidDeadline)}</span>
        )}
      </div>
    </div>
  );
}

function getDDay(deadline: string | null): number | null {
  if (!deadline) return null;
  const diff = new Date(deadline).getTime() - Date.now();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

function dDayStyle(dDay: number | null): string {
  if (dDay === null) return 'bg-gray-100 text-gray-400';
  if (dDay <= 0) return 'bg-red-100 text-red-600';
  if (dDay <= 3) return 'bg-orange-100 text-orange-600';
  return 'bg-blue-50 text-blue-600';
}

function formatBudget(budget: number): string {
  if (budget >= 100_000_000) return `${(budget / 100_000_000).toFixed(1)}억`;
  if (budget >= 10_000) return `${(budget / 10_000).toFixed(0)}만`;
  return `${budget.toLocaleString()}원`;
}

function formatDate(iso: string): string {
  return iso.slice(0, 10);
}
