import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { fetchMe, login, logout, signup } from '../api/auth';
import type { AuthUser } from '../types/auth';

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
      queryClient.setQueryData(['auth', 'me'], user);
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
      // 캐시 알림에서도 분리시킨다(TanStack Query issue #8597, works-as-designed). auth.me는
      // 라우트 전환과 무관하게 항상 마운트된 MatchStatusWatcher가 구독 중이라, removeQueries를
      // 쓰면 그 이후의 로그인 성공(setQueryData) 알림을 영원히 못 받는다.
      //
      // resetQueries로 바꾸면 observer는 유지되지만, 활성 observer가 있을 때 자동으로 /auth/me를
      // 재조회한다. 그 재조회를 기다리지 않으면 사용자가 빠르게 재로그인했을 때 뒤늦은 401
      // 응답이 새 로그인 상태를 덮어쓰는 경합이 생기고, 기다리면 axios 인스턴스에 timeout이
      // 없어(lib/axios.ts) 네트워크가 응답하지 않을 때 로그아웃 자체가 무기한 멈춘다. 그래서
      // 아예 네트워크를 타지 않는 setQueryData로 캐시를 직접 null로 채운다 — 같은 Query
      // 인스턴스의 데이터만 바꾸는 것이라 observer 연결도 끊기지 않고, 기다릴 요청도 없다.
      queryClient.setQueryData(['auth', 'me'], null);
      queryClient.removeQueries({ queryKey: ['bids'] });
      navigate('/login', { replace: true });
    },
  });
}
