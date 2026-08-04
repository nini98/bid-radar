import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { fetchMe, login, logout, signup } from '../api/auth';
import type { AuthUser } from '../types/auth';

// MatchStatusWatcher처럼 라우트 전환과 무관하게 항상 마운트된 컴포넌트가 auth.me를 계속
// observe하는 한, 이 쿼리는 로그인/로그아웃 후에도(예: /login 화면에서도) 여전히 "활성"
// 상태다. 그래서 창 포커스/재연결 재조회 등으로 이미 진행 중인 /auth/me 요청이 있을 수 있고,
// 그 상태에서 setQueryData만 호출하면 나중에 도착하는 그 요청의 응답이 지금 쓴 값을 다시
// 덮어쓴다(성공 응답이면 다른 사용자로, 실패 응답이면 isError로). cancelQueries는 그 요청의
// 내부 promise를 즉시 reject시켜 늦은 응답을 라이브러리가 조용히 버리게 한다 - 실제 네트워크
// 응답을 기다리는 게 아니라 진행 중인 fetch를 "취소됨"으로 표시만 하는 것이므로 즉시 끝난다.
function setAuthMeSafely(queryClient: QueryClient, user: AuthUser | null) {
  queryClient.cancelQueries({ queryKey: ['auth', 'me'] });
  queryClient.setQueryData(['auth', 'me'], user);
}

export function useMe() {
  return useQuery<AuthUser | null>({
    queryKey: ['auth', 'me'],
    queryFn: fetchMe,
    retry: false,
  });
}

export function useLogin() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      login(email, password),
    onSuccess: (user) => {
      setAuthMeSafely(queryClient, user);
      navigate('/', { replace: true });
    },
  });
}

export function useSignup() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: ({ email, password, name }: { email: string; password: string; name: string }) =>
      signup(email, password, name),
    onSuccess: () => {
      navigate('/login', { replace: true });
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: logout,
    onSettled: () => {
      // removeQueries는 활성 observer가 있는 쿼리를 캐시에서 완전히 제거하면서 그 observer를
      // 캐시 알림에서도 분리시킨다(TanStack Query issue #8597, works-as-designed). removeQueries를
      // 쓰면 그 이후의 로그인 성공(setAuthMeSafely) 알림을 MatchStatusWatcher가 영원히 못 받는다.
      //
      // resetQueries로 바꾸면 observer는 유지되지만, 활성 observer가 있을 때 자동으로 /auth/me를
      // 재조회한다. 그 재조회를 기다리지 않으면 경합이 생기고, 기다리면 axios 인스턴스에
      // timeout이 없어(lib/axios.ts) 네트워크가 응답하지 않을 때 로그아웃 자체가 무기한 멈춘다.
      // 그래서 네트워크를 타지 않는 setAuthMeSafely로 캐시를 직접 null로 채운다.
      setAuthMeSafely(queryClient, null);
      queryClient.removeQueries({ queryKey: ['bids'] });
      navigate('/login', { replace: true });
    },
  });
}
