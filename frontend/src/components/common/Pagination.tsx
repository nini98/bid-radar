interface Props {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({ currentPage, totalPages, onPageChange }: Props) {
  if (totalPages <= 1) return null;

  const pages = buildPageNumbers(currentPage, totalPages);

  return (
    <div className="flex items-center justify-center gap-1 mt-6">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        className="px-3 py-1.5 text-sm rounded border border-gray-200 disabled:opacity-40 hover:bg-gray-50 disabled:cursor-not-allowed"
      >
        이전
      </button>

      {pages.map((page, i) =>
        page === -1 ? (
          <span key={`ellipsis-${i}`} className="px-2 text-gray-400">
            …
          </span>
        ) : (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={`px-3 py-1.5 text-sm rounded border ${
              page === currentPage
                ? 'bg-blue-600 text-white border-blue-600'
                : 'border-gray-200 hover:bg-gray-50'
            }`}
          >
            {page + 1}
          </button>
        )
      )}

      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage >= totalPages - 1}
        className="px-3 py-1.5 text-sm rounded border border-gray-200 disabled:opacity-40 hover:bg-gray-50 disabled:cursor-not-allowed"
      >
        다음
      </button>
    </div>
  );
}

function buildPageNumbers(current: number, total: number): number[] {
  const delta = 2;
  const range: number[] = [];
  const result: number[] = [];

  for (let i = Math.max(0, current - delta); i <= Math.min(total - 1, current + delta); i++) {
    range.push(i);
  }

  if (range[0] > 0) {
    result.push(0);
    if (range[0] > 1) result.push(-1);
  }
  result.push(...range);
  if (range[range.length - 1] < total - 1) {
    if (range[range.length - 1] < total - 2) result.push(-1);
    result.push(total - 1);
  }

  return result;
}
