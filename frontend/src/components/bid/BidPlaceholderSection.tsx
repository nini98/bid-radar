interface Props {
  title: string;
}

export default function BidPlaceholderSection({ title }: Props) {
  return (
    <section className="bg-white rounded-lg border border-gray-100 p-5">
      <h2 className="text-sm font-semibold text-gray-900 mb-2">{title}</h2>
      <p className="text-sm text-gray-400">분석 준비 중입니다.</p>
    </section>
  );
}
