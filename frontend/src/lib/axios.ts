import axios from 'axios';

export interface ApiError extends Error {
  resultCode: string | null;
}

type ErrorLike = {
  response?: { data?: { data?: unknown; header?: { resultCode?: string; resultMessage?: string } } };
  message?: string;
};

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

export function resolveErrorMessage(error: ErrorLike): string {
  const body = error.response?.data;
  const detail = typeof body?.data === 'string' && body.data.trim() ? body.data : null;
  return detail ?? body?.header?.resultMessage ?? error.message ?? '오류가 발생했습니다.';
}

export function toApiError(error: ErrorLike): ApiError {
  const err = new Error(resolveErrorMessage(error)) as ApiError;
  err.resultCode = error.response?.data?.header?.resultCode ?? null;
  return err;
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});

instance.interceptors.request.use((config) => {
  const xsrf = getCookie('XSRF-TOKEN');
  if (xsrf) config.headers['X-XSRF-TOKEN'] = xsrf;
  return config;
});

instance.interceptors.response.use(
  (res) => {
    const body = res.data;
    if (body?.header?.resultCode !== '200') {
      const err = new Error(body?.header?.resultMessage ?? '오류가 발생했습니다.') as ApiError;
      err.resultCode = body?.header?.resultCode ?? null;
      throw err;
    }
    return body.data;
  },
  (error) => Promise.reject(toApiError(error))
);

export default instance;
