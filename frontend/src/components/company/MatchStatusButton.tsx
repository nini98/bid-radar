import { getMatchStatusDisplay } from '../../lib/matchStatus';
import type { MatchCalculationStatus } from '../../types/match';

interface MatchStatusButtonProps {
  status: MatchCalculationStatus | undefined;
  isLoading: boolean;
  isError: boolean;
  isRetrying: boolean;
  onRetry: () => void;
}

export default function MatchStatusButton({ status, isLoading, isError, isRetrying, onRetry }: MatchStatusButtonProps) {
  const { enabled, message } = resolveDisplay(status, isLoading, isError);
  const disabled = !enabled || isRetrying;

  return (
    <div className="flex items-center justify-between bg-white border border-gray-100 rounded-lg px-4 py-3">
      <span className="text-sm text-gray-600">{message}</span>
      <button
        type="button"
        onClick={onRetry}
        disabled={disabled}
        className="text-sm px-3 py-1.5 rounded border border-gray-200 text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-transparent"
      >
        재계산
      </button>
    </div>
  );
}

function resolveDisplay(status: MatchCalculationStatus | undefined, isLoading: boolean, isError: boolean) {
  if (isLoading) return { enabled: false, message: '확인 중...' };
  if (isError || !status) return { enabled: false, message: '상태를 불러오지 못했습니다.' };
  return getMatchStatusDisplay(status, new Date());
}
