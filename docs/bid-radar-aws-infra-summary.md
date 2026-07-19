# bid-radar AWS 인프라 요약

> 작성일: 2026-06-29 (콘솔에서 직접 구축한 내용 정리)
> 갱신: 2026-07-19 — 비용 절감을 위해 NAT Gateway / EIP / Interface VPC Endpoint 7개 삭제, EC2 중지. 인프라는 `infra/terraform/`으로 코드화 완료.
> 리전: ap-northeast-2 (서울)
> 계정 ID: 200596937528

> **참고**: 이 문서는 최초 구축 시점(6/29)의 스냅샷 기록이다. 실제 현재 상태와 리소스 정의의 source of truth는 `infra/terraform/`이다. 아래 내용 중 3장(NAT), 6장(Interface Endpoint), 8장(EC2 상태)은 갱신 이후 변경됨 — 각 섹션 하단의 갱신 노트 참고.

---

## 1. VPC

| 항목 | 값 |
|---|---|
| VPC ID | vpc-0e974aca7cbb733c4 |
| 이름 | bid-radar-vpc |
| CIDR | 10.0.0.0/16 |
| DNS resolution / DNS hostnames | 둘 다 활성화 (Private DNS Endpoint 사용을 위해 필수) |

---

## 2. 서브넷 (4개)

| 이름 | 서브넷 ID | AZ | CIDR | 용도 |
|---|---|---|---|---|
| bid-radar-public-2a | subnet-03d76dfbfc4293b2f | ap-northeast-2a | 10.0.1.0/24 | ALB, NAT Gateway |
| bid-radar-public-2c | subnet-084682701469e1edf | ap-northeast-2c | 10.0.2.0/24 | ALB (가용성 요건용) |
| bid-radar-private-2a | subnet-00b4479f4820b60fd | ap-northeast-2a | 10.0.11.0/24 | EC2, VPC Endpoint |
| bid-radar-private-2c | subnet-0f5f1fa8ad0cc2412 | ap-northeast-2c | 10.0.12.0/24 | (ALB 등록 요건용, 실사용 없음) |

---

## 3. 인터넷/NAT 게이트웨이

| 항목 | 값 |
|---|---|
| IGW ID | igw-0c506d60ad2677995 |
| IGW 이름 | bid-radar-igw |
| IGW 연결 VPC | bid-radar-vpc (Attached) |
| NAT Gateway ID | nat-0b1ffca761a8af543 |
| NAT 이름 | bid-radar-nat |
| NAT 위치 | bid-radar-public-2a |
| NAT 퍼블릭 IP (EIP) | 3.34.152.225 |
| NAT 프라이빗 IP | 10.0.1.58 |
| NAT 가용성 모드 | 영역별 (Zonal, AZ 단일) |

> **2026-07-19 갱신: NAT Gateway와 EIP는 삭제됨.** 학습/검증 끝난 뒤 비용(월 ~$47) 절감을 위해 Terraform으로 제거. IGW는 유지(무료). 재생성 필요 시 `infra/terraform/` 참고.

---

## 4. 라우팅 테이블

| 이름 | 라우팅 테이블 ID | 라우팅 규칙 | 연결된 서브넷 |
|---|---|---|---|
| bid-radar-rt-public | rtb-03116ed6b32b67a02 | 10.0.0.0/16 → local<br>0.0.0.0/0 → igw-0c506d60ad2677995 | public-2a, public-2c |
| bid-radar-rt-private | rtb-07656b03152081aa7 | 10.0.0.0/16 → local<br>0.0.0.0/0 → nat-0b1ffca761a8af543 | private-2a, private-2c |

---

## 5. 보안 그룹 (3개)

| 이름 | SG ID | 인바운드 규칙 | 용도 |
|---|---|---|---|
| bid-radar-sg-alb | sg-087ea442fcc4180a6 | HTTPS(443) ← 0.0.0.0/0 | 인터넷 → ALB |
| bid-radar-sg-ec2 | sg-00ff836a761760f14 | TCP(8080) ← sg-alb<br>TCP(80) ← sg-alb | ALB → EC2 (BE 8080, FE 80) |
| bid-radar-sg-endpoint | sg-03d97bdadd6039fc3 | HTTPS(443) ← sg-ec2 | EC2 → VPC Endpoint |

(default 보안그룹: sg-0a917f1c3dde50302, 미사용)

---

## 6. VPC Endpoint (8개, 전부 private-2a / sg-endpoint 적용)

| 이름 | 서비스 | 유형 | 비용 |
|---|---|---|---|
| bid-radar-vpce-s3 | com.amazonaws.ap-northeast-2.s3 | Gateway | 무료 (rt-private에 라우팅 연결) |
| bid-radar-vpce-secretsmanager | com.amazonaws.ap-northeast-2.secretsmanager | Interface | 유료 |
| bid-radar-vpce-ssm | com.amazonaws.ap-northeast-2.ssm | Interface | 유료 |
| bid-radar-vpce-logs | com.amazonaws.ap-northeast-2.logs | Interface | 유료 |
| bid-radar-vpce-ecr-api | com.amazonaws.ap-northeast-2.ecr.api | Interface | 유료 |
| bid-radar-vpce-ecr-dkr | com.amazonaws.ap-northeast-2.ecr.dkr | Interface | 유료 |
| bid-radar-vpce-ssmmessages | com.amazonaws.ap-northeast-2.ssmmessages | Interface | 유료 (SSM 접속용) |
| bid-radar-vpce-ec2messages | com.amazonaws.ap-northeast-2.ec2messages | Interface | 유료 (SSM 접속용) |

모든 Interface Endpoint: 프라이빗 DNS 이름 활성화 ✅

> **2026-07-19 갱신: Interface Endpoint 7개(secretsmanager/ssm/logs/ecr-api/ecr-dkr/ssmmessages/ec2messages) 전부 삭제됨.** S3 Gateway Endpoint(무료)만 유지. 이 삭제로 EC2의 SSM Session Manager 접속 경로가 사라짐(Bastion도 없어서 현재 EC2에 접속할 방법이 없음) — 재사용 시 `infra/terraform/`으로 재생성 필요.

---

## 7. IAM Role

| 항목 | 값 |
|---|---|
| 역할 이름 | bid-radar-ec2-role |
| ARN | arn:aws:iam::200596937528:instance-profile/bid-radar-ec2-role |
| 신뢰 대상 | ec2.amazonaws.com |
| 연결된 정책 | AmazonEC2ContainerRegistryReadOnly<br>AmazonSSMManagedInstanceCore<br>CloudWatchAgentServerPolicy<br>SecretsManagerReadWrite |

---

## 8. EC2 인스턴스

| 항목 | 값 |
|---|---|
| 이름 | bid-radar-ec2 |
| AMI | Amazon Linux 2023 (ami-0436b3a61a7a7e22a) |
| 인스턴스 유형 | t3.small |
| 키 페어 | jay-ubuntu-1 |
| 서브넷 | bid-radar-private-2a |
| 퍼블릭 IP | 없음 (비활성화) |
| 프라이빗 IP | 10.0.11.237 |
| 보안 그룹 | bid-radar-sg-ec2 |
| IAM 인스턴스 프로파일 | bid-radar-ec2-role |
| 스토리지 | 20GiB gp3 |
| 접속 방식 | ~~SSM Session Manager (Bastion 없음)~~ → 2026-07-19부로 접속 불가 (아래 참고) |

### EC2 내부 소프트웨어 설치 현황
- Docker 25.0.14 (설치 완료)
- Docker Compose v5.2.0 (CLI 플러그인, 설치 완료)

> **2026-07-19 갱신: 인스턴스 중지(stopped) 상태.** NAT/SSM 관련 Endpoint 삭제로 어차피 접속 경로가 없어져서 컴퓨팅 비용(월 ~$19)까지 절감 목적으로 중지. 인스턴스 자체(디스크 포함)는 살아있어 필요 시 콘솔/CLI로 재시작 가능하나, 재시작해도 NAT/Endpoint를 다시 만들기 전까진 SSM 접속은 여전히 안 됨.

---

## 9. ECR Private Repository (3개)

| 리포지토리 이름 | URI |
|---|---|
| bid-radar/backend | 200596937528.dkr.ecr.ap-northeast-2.amazonaws.com/bid-radar/backend |
| bid-radar/frontend | 200596937528.dkr.ecr.ap-northeast-2.amazonaws.com/bid-radar/frontend |
| bid-radar/ai-worker | 200596937528.dkr.ecr.ap-northeast-2.amazonaws.com/bid-radar/ai-worker |

태그 변경 가능성: Mutable / 암호화: AES-256

---

## 10. 트래픽 흐름 요약 (2026-06-29 최초 설계 기준, 아래 갱신 노트 참고)

```
인터넷
  │ (443)
  ▼
[ALB] (public-2a/2c, sg-alb)  ※ 아직 미생성
  │ (8080/80)
  ▼
[EC2 bid-radar-ec2] (private-2a, sg-ec2)
  │ (443, NAT 안 거침)
  ▼
[VPC Endpoint] (private-2a, sg-endpoint)
  │
  ▼
AWS 서비스 (Secrets Manager / SSM / ECR / CloudWatch Logs / S3)

EC2의 일반 인터넷 아웃바운드(apt, GitHub 등)는 NAT Gateway(public-2a)를 거침
```

> **2026-07-19 갱신: 위 흐름 중 NAT Gateway와 VPC Endpoint(S3 제외) 구간은 현재 존재하지 않음.** EC2도 중지 상태라 이 트래픽 흐름 자체가 지금은 작동하지 않는다. 재배포 검증 시 `infra/terraform/`으로 재생성 후 이 흐름이 복원된다.

---

## 11. 아직 안 한 작업 (TODO)

- [ ] Secrets Manager / Parameter Store에 실제 시크릿·설정값 등록
- [ ] docker-compose.yml 작성 (BE/FE/AI/DB)
- [ ] ALB + Target Group 생성 (도메인/ACM 인증서 필요)
- [ ] GitHub Actions CI/CD 파이프라인 (build → ECR push → EC2 pull/배포)
- [ ] 전체 배포 동작 확인
- [x] 학습/검증 끝나면 NAT, Interface Endpoint 등 비용 리소스 정리 (2026-07-19 완료, ALB는 애초에 미생성이라 해당 없음)
- [x] Terraform으로 코드화 (2026-07-19 완료, `infra/terraform/`)

---

## 비용 관련 참고

- NAT Gateway: 시간당 + 데이터 처리량 과금 (약 $42/월 고정 + 트래픽) — **2026-07-19 삭제됨**
- Interface Endpoint 7개(secretsmanager/ssm/logs/ecr-api/ecr-dkr/ssmmessages/ec2messages): 각각 시간당 과금 — **2026-07-19 전부 삭제됨**
- Elastic IP(NAT용): 연결 여부 무관하게 시간당 과금(2024.2~ AWS 정책) — **2026-07-19 삭제됨**
- S3 Endpoint(Gateway): 무료 — 유지 중
- EC2 t3.small: 시간당 과금 — **2026-07-19 중지(stopped)**, EBS 스토리지 비용만 남음
- ALB: 애초에 미생성 상태로 비용 발생한 적 없음
- 위 정리로 월 추정 비용이 ~$135 → ~$2(EBS만) 수준으로 감소
- 다시 배포 검증이 필요해지면 `infra/terraform/`에서 NAT/Endpoint 리소스를 되살리고(`terraform apply`) EC2를 시작하면 됨
