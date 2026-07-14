import type { CodeItem } from '../../types/code';
import MultiSelect from './MultiSelect';

interface Props {
  options: CodeItem[];
  selectedIds: number[];
  onChange: (ids: number[]) => void;
}

export default function TechTagSelect({ options, selectedIds, onChange }: Props) {
  return (
    <MultiSelect
      label="기술 태그"
      options={options.map((o) => ({ value: String(o.id), label: o.name }))}
      selected={selectedIds.map(String)}
      onChange={(next) => onChange(next.map(Number))}
    />
  );
}
