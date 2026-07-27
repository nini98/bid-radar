// 나라장터 조달청 OpenAPI 명세서(`docs/조달청_OpenAPI참고자료_나라장터_입찰공고정보서비스_1.2.docx`,
// getBidPblancListInfoServc 응답의 bidMethdNm) 기준 입찰방식 값 목록 (Issue #19에서 검증).
export const BID_TYPES = [
  '전자입찰', '직찰', '전자입찰/직찰', '전자/직찰/우편/상시', '직찰/우편/상시', '우편/상시',
  '전자시담', '전자시담(다자간)', '복수견적(역경매)', '직찰/우편',
];

// 같은 명세서의 cntrctCnclsMthdNm(계약방식) 값 목록.
export const CONTRACT_TYPES = ['일반경쟁', '제한경쟁', '지명경쟁', '수의계약'];
