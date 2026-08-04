import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { fetchMe, login, logout, signup } from '../api/auth';

export function useMe() {
  return useQuery({
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
      // 쓰면 그 이후의 로그인 성공(setQueryData) 알림을 영원히 못 받는다. resetQueries는 같은
      // Query 인스턴스를 유지한 채 초기 상태로 되돌려 observer 연결이 끊기지 않는다.
      queryClient.resetQueries({ queryKey: ['auth'] });
      queryClient.removeQueries({ queryKey: ['bids'] });
      navigate('/login', { replace: true });
    },
  });
}
