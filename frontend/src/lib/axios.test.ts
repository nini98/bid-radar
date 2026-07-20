import { describe, expect, it } from 'vitest';
import { resolveErrorMessage, toApiError } from './axios';

describe('resolveErrorMessage', () => {
  it('필드별 상세 메시지(data가 문자열)가 있으면 그것을 우선 사용한다', () => {
    const error = {
      response: {
        data: { data: '이름은 필수입니다.', header: { resultMessage: '입력한 값이 유효하지 않습니다.' } },
      },
    };

    expect(resolveErrorMessage(error)).toBe('이름은 필수입니다.');
  });

  it('data가 없으면 header.resultMessage로 폴백한다', () => {
    const error = {
      response: { data: { header: { resultMessage: '입력한 값이 유효하지 않습니다.' } } },
    };

    expect(resolveErrorMessage(error)).toBe('입력한 값이 유효하지 않습니다.');
  });

  it('data가 공백 문자열이면 상세 메시지로 취급하지 않고 폴백한다', () => {
    const error = {
      response: { data: { data: '   ', header: { resultMessage: '입력한 값이 유효하지 않습니다.' } } },
    };

    expect(resolveErrorMessage(error)).toBe('입력한 값이 유효하지 않습니다.');
  });

  it('response 자체가 없으면 error.message로 폴백한다', () => {
    const error = { message: '네트워크 오류' };

    expect(resolveErrorMessage(error)).toBe('네트워크 오류');
  });

  it('아무 정보도 없으면 기본 메시지를 반환한다', () => {
    expect(resolveErrorMessage({})).toBe('오류가 발생했습니다.');
  });
});

describe('toApiError', () => {
  it('404 응답이면 resultCode가 "404"로 설정된다', () => {
    const error = {
      response: {
        data: { header: { resultCode: '404', resultMessage: '리소스를 찾을 수 없습니다.' } },
      },
    };

    const apiError = toApiError(error);

    expect(apiError.resultCode).toBe('404');
    expect(apiError.message).toBe('리소스를 찾을 수 없습니다.');
  });

  it('response 자체가 없으면(네트워크 에러 등) resultCode가 null이다', () => {
    const apiError = toApiError({ message: '네트워크 오류' });

    expect(apiError.resultCode).toBeNull();
    expect(apiError.message).toBe('네트워크 오류');
  });
});
