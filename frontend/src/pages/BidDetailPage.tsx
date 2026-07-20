import { Link, useParams } from 'react-router-dom';
import { useBidDetail } from '../hooks/useBidDetail';
import type { ApiError } from '../lib/axios';
import BidDetailInfoSection from '../components/bid/BidDetailInfoSection';
import BidMatchSection from '../components/bid/BidMatchSection';
import BidScheduleSection from '../components/bid/BidScheduleSection';
import BidAttachmentSection from '../components/bid/BidAttachmentSection';
import BidPlaceholderSection from '../components/bid/BidPlaceholderSection';
import BidDetailSkeleton from '../components/bid/BidDetailSkeleton';

export default function BidDetailPage() {
  const { bidId } = useParams<{ bidId: string }>();
  const numericBidId = Number(bidId);

  const { data: bid, isLoading, isError, error, refetch } = useBidDetail(numericBidId);
  const isNotFound = isError && (error as ApiError).resultCode === '404';

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 h-12 flex items-center">
          <Link to="/" className="text-sm text-gray-500 hover:text-gray-800">
            ← 목록으로
          </Link>
        </div>
      </header>

      <div className="max-w-4xl mx-auto px-4 py-6 space-y-4">
        {isLoading && <BidDetailSkeleton />}

        {isNotFound && (
          <div className="text-center py-16 text-gray-500">
            <p className="mb-3">공고를 찾을 수 없습니다.</p>
            <Link to="/" className="text-sm text-blue-600 hover:underline">
              목록으로 돌아가기
            </Link>
          </div>
        )}

        {isError && !isNotFound && (
          <div className="text-center py-16 text-gray-500">
            <p className="mb-3">{error instanceof Error ? error.message : '오류가 발생했습니다.'}</p>
            <button
              onClick={() => refetch()}
              className="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              다시 시도
            </button>
          </div>
        )}

        {!isLoading && !isError && bid && (
          <>
            <BidDetailInfoSection bid={bid} />
            <BidMatchSection matchResult={bid.matchResult} />
            <BidScheduleSection bid={bid} />
            <BidAttachmentSection attachments={bid.attachments} />
            <BidPlaceholderSection title="AI 요약" />
            <BidPlaceholderSection title="위험 요인" />
            <BidPlaceholderSection title="유사 공고" />
          </>
        )}
      </div>
    </div>
  );
}
