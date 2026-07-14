import { useState } from 'react';

interface Props {
  items: string[];
  onChange: (next: string[]) => void;
}

export default function CertificateList({ items, onChange }: Props) {
  const [input, setInput] = useState('');

  const add = () => {
    const value = input.trim();
    if (!value || items.includes(value)) return;
    onChange([...items, value]);
    setInput('');
  };

  const remove = (index: number) => {
    onChange(items.filter((_, i) => i !== index));
  };

  return (
    <div>
      <h2 className="text-sm font-semibold text-gray-900 mb-3">보유 인증</h2>
      <div className="flex gap-2 mb-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              add();
            }
          }}
          placeholder="인증명 입력 후 추가"
          className="flex-1 text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
        />
        <button
          type="button"
          onClick={add}
          className="px-3 py-1.5 text-xs bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
        >
          추가
        </button>
      </div>
      <div className="flex flex-wrap gap-2">
        {items.map((item, i) => (
          <span key={i} className="flex items-center gap-1 px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded">
            {item}
            <button type="button" onClick={() => remove(i)} className="text-gray-400 hover:text-red-500">
              ×
            </button>
          </span>
        ))}
      </div>
    </div>
  );
}
