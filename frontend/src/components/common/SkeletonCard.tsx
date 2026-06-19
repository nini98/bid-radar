export default function SkeletonCard() {
  return (
    <div className="bg-white rounded-lg border border-gray-100 p-4 animate-pulse">
      <div className="flex justify-between items-start mb-3">
        <div className="h-4 bg-gray-200 rounded w-3/4" />
        <div className="h-6 bg-gray-200 rounded w-12" />
      </div>
      <div className="h-3 bg-gray-200 rounded w-1/2 mb-4" />
      <div className="flex gap-4">
        <div className="h-3 bg-gray-200 rounded w-20" />
        <div className="h-3 bg-gray-200 rounded w-20" />
        <div className="h-3 bg-gray-200 rounded w-20" />
      </div>
    </div>
  );
}
