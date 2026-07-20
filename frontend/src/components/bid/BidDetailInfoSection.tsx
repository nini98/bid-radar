import type { BidNoticeDetail } from '../../types/bid';
import { formatBudget } from '../../lib/format';

interface Props {
  bid: BidNoticeDetail;
}

export default function BidDetailInfoSection({ bid }: Props) {
  return (
    <section className="bg-white rounded-lg border border-gray-100 p-5">
      <h1 className="text-lg font-semibold text-gray-900 mb-3">{bid.title}</h1>

      <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
        <div>
          <dt className="text-gray-400">발주 기관</dt>
          <dd className="text-gray-800">{bid.agency ?? '기관 미상'}</dd>
        </div>
        <div>
          <dt className="text-gray-400">예산</dt>
          <dd className="text-gray-800">{bid.budget !== null ? formatBudget(bid.budget) : '-'}</dd>
        </div>
        <div>
          <dt className="text-gray-400">지역</dt>
          <dd className="text-gray-800">{bid.region ?? '-'}</dd>
        </div>
        <div>
          <dt className="text-gray-400">입찰 방식</dt>
          <dd className="text-gray-800">{bid.bidType ?? '-'}</dd>
        </div>
        <div>
          <dt className="text-gray-400">계약 방식</dt>
          <dd className="text-gray-800">{bid.contractType ?? '-'}</dd>
        </div>
        <div>
          <dt className="text-gray-400">지역 제한</dt>
          <dd className="text-gray-800">{bid.regionRestriction ?? '-'}</dd>
        </div>
      </dl>

      {bid.qualificationSummary && (
        <div className="mt-4 pt-4 border-t border-gray-100">
          <dt className="text-gray-400 text-sm mb-1">자격요건 요약</dt>
          <dd className="text-sm text-gray-700 whitespace-pre-line">{bid.qualificationSummary}</dd>
        </div>
      )}

      {bid.noticeUrl && (
        <a
          href={bid.noticeUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-block mt-4 text-sm text-blue-600 hover:underline"
        >
          나라장터 원문 보기 →
        </a>
      )}
    </section>
  );
}
