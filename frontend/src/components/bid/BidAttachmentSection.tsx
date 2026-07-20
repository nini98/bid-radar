import type { BidAttachment } from '../../types/bid';

interface Props {
  attachments: BidAttachment[];
}

export default function BidAttachmentSection({ attachments }: Props) {
  return (
    <section className="bg-white rounded-lg border border-gray-100 p-5">
      <h2 className="text-sm font-semibold text-gray-900 mb-3">첨부파일</h2>

      {attachments.length === 0 ? (
        <p className="text-sm text-gray-400">첨부파일이 없습니다.</p>
      ) : (
        <ul className="space-y-1.5">
          {attachments.map((file) => (
            <li key={file.id} className="text-sm">
              {file.downloadUrl ? (
                <a
                  href={file.downloadUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:underline"
                >
                  📎 {file.fileName}
                </a>
              ) : (
                <span className="text-gray-400">📎 {file.fileName} (다운로드 링크 없음)</span>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
