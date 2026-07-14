import { useEffect } from 'react';

interface Props {
  message: string;
  type: 'success' | 'error';
  onDismiss: () => void;
}

export default function Toast({ message, type, onDismiss }: Props) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, 3000);
    return () => clearTimeout(timer);
  }, [onDismiss]);

  const style = type === 'success'
    ? 'bg-green-600 text-white'
    : 'bg-red-600 text-white';

  return (
    <div className={`fixed bottom-6 right-6 z-50 px-4 py-3 rounded-lg shadow-lg text-sm ${style}`}>
      {message}
    </div>
  );
}
