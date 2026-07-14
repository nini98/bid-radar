import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useBidList } from '../hooks/useBidList';
import { useMe, useLogout } from '../hooks/useAuth';
import BidCard from '../components/bid/BidCard';
import BidSearchBar from '../components/bid/BidSearchBar';
import BidSummaryBar from '../components/bid/BidSummaryBar';
import Pagination from '../components/common/Pagination';
import SkeletonCard from '../components/common/SkeletonCard';
import type { BidSortType } from '../types/bid';

export default function BidListPage() {
  const [keyword, setKeyword] = useState('');
  const [region, setRegion] = useState('');
  const [sort, setSort] = useState<BidSortType>('LATEST');
  const [page, setPage] = useState(0);

  const { data: user } = useMe();
  const { mutate: logout, isPending: isLoggingOut } = useLogout();

  const { data, isLoading, isError, error, refetch } = useBidList({
    keyword: keyword || undefined,
    region: region || undefined,
    sort,
    page,
    size: 20,
  });

  const handleSearch = () => setPage(0);
  const handlePageChange = (p: number) => {
    setPage(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 h-12 flex items-center justify-between">
          <span className="font-bold text-gray-900">Bid Radar</span>
          <div className="flex items-center gap-3 text-sm text-gray-500">
            {user && <span>{user.name}</span>}
            <Link to="/company/profile" className="text-gray-400 hover:text-gray-700">
              회사 프로필
            </Link>
            <button
              onClick={() => logout()}
              disabled={isLoggingOut}
              className="text-gray-400 hover:text-gray-700 disabled:opacity-50"
            >
              로그아웃
            </button>
          </div>
        </div>
      </header>

      <div className="max-w-4xl mx-auto px-4 py-6">
        <BidSearchBar
          keyword={keyword}
          sort={sort}
          region={region}
          onKeywordChange={setKeyword}
          onSortChange={(v) => { setSort(v); setPage(0); }}
          onRegionChange={(v) => { setRegion(v); setPage(0); }}
          onSearch={handleSearch}
        />

        {data && (
          <BidSummaryBar summary={data.summary} totalElements={data.page.totalElements} />
        )}

        {isLoading && (
          <div className="space-y-3">
            {Array.from({ length: 8 }).map((_, i) => (
              <SkeletonCard key={i} />
            ))}
          </div>
        )}

        {isError && (
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

        {!isLoading && !isError && data && (
          <>
            {data.content.length === 0 ? (
              <div className="text-center py-16 text-gray-400">
                <p className="text-3xl mb-3">🔍</p>
                <p>검색 결과가 없습니다.</p>
              </div>
            ) : (
              <div className="space-y-3">
                {data.content.map((bid) => (
                  <BidCard key={bid.id} bid={bid} />
                ))}
              </div>
            )}

            <Pagination
              currentPage={page}
              totalPages={data.page.totalPages}
              onPageChange={handlePageChange}
            />
          </>
        )}
      </div>
    </div>
  );
}
