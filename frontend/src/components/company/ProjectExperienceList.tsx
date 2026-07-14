import { useState } from 'react';
import type { ProjectExperience } from '../../types/company';

interface Props {
  items: ProjectExperience[];
  onChange: (next: ProjectExperience[]) => void;
}

export default function ProjectExperienceList({ items, onChange }: Props) {
  const [projectType, setProjectType] = useState('');
  const [description, setDescription] = useState('');

  const add = () => {
    if (!projectType.trim() || !description.trim()) return;
    onChange([...items, { projectType: projectType.trim(), description: description.trim() }]);
    setProjectType('');
    setDescription('');
  };

  const remove = (index: number) => {
    onChange(items.filter((_, i) => i !== index));
  };

  return (
    <div>
      <h2 className="text-sm font-semibold text-gray-900 mb-3">프로젝트 경험</h2>
      <div className="flex gap-2 mb-2">
        <input
          type="text"
          value={projectType}
          onChange={(e) => setProjectType(e.target.value)}
          placeholder="프로젝트 구분 (예: 공공 SI)"
          className="w-40 text-sm border border-gray-200 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-blue-400"
        />
        <input
          type="text"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="프로젝트 설명"
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
      <div className="space-y-1">
        {items.map((item, i) => (
          <div key={i} className="flex items-center justify-between text-xs bg-gray-50 rounded px-3 py-2">
            <span>
              <span className="font-medium text-gray-700">{item.projectType}</span> · {item.description}
            </span>
            <button type="button" onClick={() => remove(i)} className="text-gray-400 hover:text-red-500 ml-2">
              ×
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
