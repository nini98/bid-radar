import { describe, expect, it, vi, beforeEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useMe, useLogin, useLogout } from './useAuth';
import * as authApi from '../api/auth';
import type { AuthUser } from '../types/auth';

vi.mock('../api/auth');

const userA: AuthUser = { id: 1, email: 'a@test.com', name: '사용자A', role: 'USER' };
const userB: AuthUser = { id: 2, email: 'b@test.com', name: '사용자B', role: 'USER' };

function createWrapper(queryClient: QueryClient) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>{children}</MemoryRouter>
      </QueryClientProvider>
    );
  };
}

describe('useLogout', () => {
  beforeEach(() => {
    vi.mocked(authApi.fetchMe).mockResolvedValue(userA);
    vi.mocked(authApi.logout).mockResolvedValue(undefined);
    vi.mocked(authApi.login).mockResolvedValue(userB);
  });

  // 이슈 #48 회귀 테스트: MatchStatusWatcher처럼 라우트 전환과 무관하게 앱 생명주기 동안 계속
  // 마운트된 컴포넌트의 useMe() observer가, 로그아웃 이후 다른 사용자로 로그인해도 정상적으로
  // 갱신되는지 확인한다. removeQueries(관찰자 분리 문제)나 await 없는 resetQueries(경합
  // 문제)로 되돌아가면 이 테스트가 깨진다.
  it('항상 마운트된 useMe() observer가 로그아웃 후 재로그인 시 새 사용자로 갱신된다', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const wrapper = createWrapper(queryClient);

    const probe = renderHook(() => useMe(), { wrapper });
    await waitFor(() => expect(probe.result.current.data).toEqual(userA));

    const logoutHook = renderHook(() => useLogout(), { wrapper });
    await act(async () => {
      await logoutHook.result.current.mutateAsync();
    });
    await waitFor(() => expect(probe.result.current.data).toBeNull());

    const loginHook = renderHook(() => useLogin(), { wrapper });
    await act(async () => {
      await loginHook.result.current.mutateAsync({ email: 'b@test.com', password: 'password123' });
    });
    await waitFor(() => expect(probe.result.current.data).toEqual(userB));
  });
});
