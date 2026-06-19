import { QueryClient } from '@tanstack/react-query';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60,
      retry: (failureCount, error) => {
        if (error instanceof Error && error.message.includes('인증')) return false;
        return failureCount < 1;
      },
    },
  },
});

export default queryClient;
