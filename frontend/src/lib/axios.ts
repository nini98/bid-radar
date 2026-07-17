import axios from 'axios';

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

export function resolveErrorMessage(error: {
  response?: { data?: { data?: unknown; header?: { resultMessage?: string } } };
  message?: string;
}): string {
  const body = error.response?.data;
  const detail = typeof body?.data === 'string' && body.data.trim() ? body.data : null;
  return detail ?? body?.header?.resultMessage ?? error.message ?? '오류가 발생했습니다.';
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
      throw new Error(body?.header?.resultMessage ?? '오류가 발생했습니다.');
    }
    return body.data;
  },
  (error) => Promise.reject(new Error(resolveErrorMessage(error)))
);

export default instance;
