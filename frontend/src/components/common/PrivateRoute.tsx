import { Navigate } from 'react-router-dom';
import { useMe } from '../../hooks/useAuth';

interface Props {
  children: React.ReactNode;
}

export default function PrivateRoute({ children }: Props) {
  const { data: user, isLoading, isError } = useMe();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (isError || !user) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
