# EPIC-01 Docker Compose 구성 및 EC2 배포

## 목표

Docker 이미지를 ECR에 push하고, GitHub Actions CI/CD로 EC2에 자동 배포한다.

---

## 파트

Spring Boot + Frontend + Infra

---

## 참조 Rule

- `docs/rules/springboot/config-property-rule.md`

---

## 배포 아키텍처

```
GitHub push (main)
    ↓
[GitHub Actions]
  → docker build (backend, frontend)
  → ECR push
  → SSM Run Command → EC2: docker pull + docker compose up -d

EC2 (private subnet, SSM 접속)
  → ECR에서 이미지 pull (VPC Endpoint 경유)
  → docker-compose.prod.yml로 컨테이너 실행
```

> 초기 계획(git clone + EC2 직접 빌드)에서 변경됨.
> AWS 인프라 구성(private subnet, ECR, VPC Endpoint) 확인 후 ECR + CI/CD 방식이 적합하다고 판단.
> 상세 인프라 구성: `docs/bid-radar-aws-infra-summary.md` 참조

---

## Done Condition

- [ ] GitHub Actions로 main push 시 ECR에 이미지가 자동 push된다
- [ ] EC2에서 ECR 이미지를 pull해 컨테이너가 실행된다
- [ ] 환경 변수가 EC2의 `.env.prod` 파일로 외부 주입된다
- [ ] EC2에서 서비스가 정상 동작한다 (ALB 또는 포트포워딩으로 접근 확인)
- [ ] 공고 목록 화면이 배포 환경에서 정상 동작한다

---

## Out of Scope

- HTTPS / SSL / ALB → Epic-4
- 모니터링 / 로그 수집 → Epic-4
- Secrets Manager 연동 → Epic-4

---

## 작업 항목

### 완료
- [x] `backend/Dockerfile` 작성 (멀티스테이지 빌드)
- [x] `frontend/Dockerfile` 작성 (빌드 + Nginx 서빙)
- [x] `frontend/nginx.conf` 작성 (`/api/*` → backend 프록시)
- [x] `.env.prod.example` 작성

### 진행 중
- [ ] `infra/docker-compose.prod.yml` 수정 — `build:` → `image:` (ECR URI)
- [ ] GitHub Actions workflow 작성
  - main push 트리거
  - backend/frontend 이미지 빌드 및 ECR push
  - SSM Run Command로 EC2 배포
- [ ] GitHub Secrets 등록 (AWS 자격증명)
- [ ] EC2 초기 세팅 (SSM 접속)
  - `/app/.env.prod` 파일 생성
  - `/app/docker-compose.prod.yml` 파일 업로드
- [ ] 첫 배포 동작 확인
