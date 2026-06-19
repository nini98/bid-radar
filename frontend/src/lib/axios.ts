import axios from 'axios';

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

const instance = axios.create({
  baseURL: '/api',
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
  (error) => {
    const message =
      error.response?.data?.header?.resultMessage ?? error.message ?? '오류가 발생했습니다.';
    return Promise.reject(new Error(message));
  }
);

export default instance;
