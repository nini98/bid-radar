import { useEffect, useState } from 'react';
import { getMatchStatusDisplay } from '../../lib/matchStatus';
import type { MatchCalculationStatus } from '../../types/match';

const STALE_CHECK_INTERVAL_MS = 5000;

interface MatchStatusButtonProps {
  status: MatchCalculationStatus | undefined;
  isLoading: boolean;
  isError: boolean;
  isRetrying: boolean;
  onRetry: () => void;
  onRefetch: () => void;
}

export default function MatchStatusButton({
  status,
  isLoading,
  isError,
  isRetrying,
  onRetry,
  onRefetch,
}: MatchStatusButtonProps) {
  // IN_PROGRESS 응답이 동일한 값으로 반복되면 structural sharing으로 리렌더가 일어나지 않아
  // 5분 경과(stale) 판정이 갱신되지 않는다. 주기적으로 강제 리렌더해서 재평가한다.
  const [, forceTick] = useState(0);
  useEffect(() => {
    if (status?.status !== 'IN_PROGRESS') return;
    const id = setInterval(() => forceTick((t) => t + 1), STALE_CHECK_INTERVAL_MS);
    return () => clearInterval(id);
  }, [status?.status]);

  if (isLoading) {
    return <StatusRow message="확인 중..." buttonLabel="재계산" disabled onClick={onRetry} />;
  }

  if (isError || !status) {
    return <StatusRow message="상태를 불러오지 못했습니다." buttonLabel="다시 확인" disabled={false} onClick={onRefetch} />;
  }

  const { enabled, message } = getMatchStatusDisplay(status, new Date());
  return <StatusRow message={message} buttonLabel="재계산" disabled={!enabled || isRetrying} onClick={onRetry} />;
}

interface StatusRowProps {
  message: string;
  buttonLabel: string;
  disabled: boolean;
  onClick: () => void;
}

function StatusRow({ message, buttonLabel, disabled, onClick }: StatusRowProps) {
  return (
    <div className="flex items-center justify-between bg-white border border-gray-100 rounded-lg px-4 py-3">
      <span className="text-sm text-gray-600">{message}</span>
      <button
        type="button"
        onClick={onClick}
        disabled={disabled}
        className="text-sm px-3 py-1.5 rounded border border-gray-200 text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-transparent"
      >
        {buttonLabel}
      </button>
    </div>
  );
}
