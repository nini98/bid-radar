# EPIC-01 Docker Compose 구성 및 개발 서버 배포

## 목표

Docker Compose로 전체 서비스(backend + frontend + PostgreSQL)를 구성하고 개발 서버(EC2)에 초기 배포한다.

---

## 파트

Spring Boot + Frontend + Infra

---

## 참조 Rule

- `docs/rules/springboot/config-property-rule.md`

---

## Done Condition

- [ ] `docker-compose up`으로 backend + frontend + PostgreSQL이 함께 실행된다
- [ ] 환경 변수가 `.env` 파일로 외부 주입된다
- [ ] 개발 서버(EC2)에서 브라우저로 서비스에 접근할 수 있다
- [ ] 공고 목록 화면이 개발 서버에서 정상 동작한다

---

## Out of Scope

- GitHub Actions CI/CD 자동화 → Epic-4
- 운영 환경 배포 → Epic-4
- SSL / HTTPS → Epic-4
- 모니터링 / 로그 수집 → Epic-4

---

## 작업 항목

- [ ] `backend/Dockerfile` 작성 (멀티 스테이지 빌드)
- [ ] `frontend/Dockerfile` 작성 (빌드 + Nginx 서빙)
- [ ] `infra/docker-compose.yml` 작성
  - services: backend, frontend, postgresql
  - 환경 변수 `.env` 참조
  - 컨테이너 간 네트워크 설정
- [ ] `.env.example` 작성 (민감정보 제외 키 목록)
- [ ] EC2 서버 초기 설정
  - Docker, Docker Compose 설치
  - 방화벽(보안 그룹) 설정 (80, 443, 8080 포트)
- [ ] 개발 서버 배포 및 동작 확인
