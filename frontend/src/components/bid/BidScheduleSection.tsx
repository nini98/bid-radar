import type { BidNoticeDetail } from '../../types/bid';
import { formatDateTime } from '../../lib/format';

interface Props {
  bid: BidNoticeDetail;
}

export default function BidScheduleSection({ bid }: Props) {
  const items: { label: string; value: string | null }[] = [
    { label: '질의 마감', value: bid.questionDeadline },
    { label: '서류 마감', value: bid.documentDeadline },
    { label: '입찰 마감', value: bid.bidDeadline },
    { label: '개찰일', value: bid.openAt },
  ];

  return (
    <section className="bg-white rounded-lg border border-gray-100 p-5">
      <h2 className="text-sm font-semibold text-gray-900 mb-3">주요 일정</h2>
      <ul className="grid grid-cols-2 gap-y-2 text-sm">
        {items.map((item) => (
          <li key={item.label} className="flex justify-between pr-4">
            <span className="text-gray-400">{item.label}</span>
            <span className="text-gray-800">{item.value ? formatDateTime(item.value) : '-'}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
