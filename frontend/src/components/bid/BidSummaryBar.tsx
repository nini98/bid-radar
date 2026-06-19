import type { BidListSummary } from '../../types/bid';

interface Props {
  summary: BidListSummary;
  totalElements: number;
}

export default function BidSummaryBar({ summary, totalElements }: Props) {
  return (
    <div className="flex items-center justify-between mb-3 text-sm text-gray-500">
      <span>전체 <strong className="text-gray-900">{totalElements.toLocaleString()}</strong>건</span>
      <div className="flex gap-4">
        <span>오늘 신규 <strong className="text-blue-600">{summary.todayNewCount}</strong>건</span>
        <span>오늘 마감 <strong className="text-red-500">{summary.todayDeadlineCount}</strong>건</span>
      </div>
    </div>
  );
}
