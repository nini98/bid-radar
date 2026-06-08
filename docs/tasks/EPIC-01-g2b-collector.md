# EPIC-01 나라장터 공고 수집 (G2B API 연동 + 스케줄러)

## 목표

공공데이터포털 나라장터 API를 연동하여 입찰 공고를 수집하고 DB에 저장하는 스케줄러를 구현한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/entity-rule.md`
- `docs/rules/springboot/repository-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/config-property-rule.md`
- `docs/rules/springboot/logging-rule.md`

---

## Done Condition

- [ ] 스케줄러 실행 시 나라장터 API에서 공고 목록이 수집된다
- [ ] 수집된 공고가 `bid_notices` 테이블에 저장된다
- [ ] 중복 공고(`external_notice_id` 기준)는 저장하지 않고 스킵된다
- [ ] 첨부파일 정보가 `bid_attachments` 테이블에 저장된다
- [ ] API 호출 실패 시 에러 로그가 기록되고 앱이 중단되지 않는다

---

## Out of Scope

- S3 첨부파일 다운로드 → 추후
- 적합도 점수 계산 → Epic-2
- AI 문서 분석 → Epic-3
- collection_jobs 수집 이력 관리 → Epic-4
- 수집 실패 재시도 → Epic-4

---

## 작업 항목

- [ ] `G2bProperties` 설정 클래스 구현 (`app.g2b.*` prefix)
  - serviceKey, baseUrl, pageSize 등
- [ ] `G2bApiClient` 구현 (RestClient)
  - 입찰 공고 목록 조회 호출
  - 응답 파싱
- [ ] G2B API 응답 DTO 작성 (`G2bNoticeResponse`, `G2bAttachmentResponse`)
- [ ] `BidNoticeRepository` 구현
  - `existsByExternalNoticeId` (중복 체크)
- [ ] `BidAttachmentRepository` 구현
- [ ] `BidCollectorService` 구현
  - API 호출 → DTO 매핑 → 중복 제거 → 저장
  - 트랜잭션 범위: 공고 1건 단위
- [ ] `BidCollectScheduler` 구현
  - `@Scheduled` cron 설정 (매일 오전 수집)
  - 실패 시 에러 로그 기록, 다음 실행에 영향 없음
