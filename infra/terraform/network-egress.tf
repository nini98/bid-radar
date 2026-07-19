# NAT Gateway / EIP / Interface VPC Endpoint 7개는 실사용 트래픽 없이 상시 과금돼서
# 2026-07-19에 삭제했다 (PR #31, docs/bid-radar-aws-infra-summary.md 참고).
# git 히스토리에서 복원하는 방식은 불가능하다 — 이 리소스들이 존재하던 시점의
# generated.tf는 한 번도 커밋된 적이 없다 (로컬 작업 중에만 존재했다가 첫 커밋
# 전에 이미 삭제됨). 대신 여기 코드로 남겨두고 var.enable_network_egress로
# 켜고 끄는 방식으로 관리한다.
#
# 복원 방법: var.enable_network_egress = true 로 설정하고 terraform apply
# (신규 리소스로 생성됨 — 예전 것과 ID는 다르지만 기능은 동일)

resource "aws_eip" "nat" {
  count  = var.enable_network_egress ? 1 : 0
  domain = "vpc"

  tags = {
    Name = "bid-radar-nat-eip"
  }
}

resource "aws_nat_gateway" "main" {
  count             = var.enable_network_egress ? 1 : 0
  allocation_id     = aws_eip.nat[0].id
  connectivity_type = "public"
  subnet_id         = aws_subnet.public_2a.id

  tags = {
    Name = "bid-radar-nat"
  }
}

resource "aws_vpc_endpoint" "secretsmanager" {
  count               = var.enable_network_egress ? 1 : 0
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.ap-northeast-2.secretsmanager"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [aws_subnet.private_2a.id]
  security_group_ids  = [aws_security_group.endpoint.id]
  private_dns_enabled = true

  tags = {
    Name = "bid-radar-vpce-secretsmanager"
  }
}

resource "aws_vpc_endpoint" "ssm" {
  count               = var.enable_network_egress ? 1 : 0
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.ap-northeast-2.ssm"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [aws_subnet.private_2a.id]
  security_group_ids  = [aws_security_group.endpoint.id]
  private_dns_enabled = true

  tags = {
    Name = "bid-radar-vpce-ssm"
  }
}

resource "aws_vpc_endpoint" "logs" {
  count               = var.enable_network_egress ? 1 : 0
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.ap-northeast-2.logs"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [aws_subnet.private_2a.id]
  security_group_ids  = [aws_security_group.endpoint.id]
  private_dns_enabled = true

  tags = {
    Name = "bid-radar-vpce-logs"
  }
}

resource "aws_vpc_endpoint" "ecr_api" {
  count               = var.enable_network_egress ? 1 : 0
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.ap-northeast-2.ecr.api"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [aws_subnet.private_2a.id]
  security_group_ids  = [aws_security_group.endpoint.id]
  private_dns_enabled = true

  tags = {
    Name = "bid-radar-vpce-ecr-api"
  }
}

resource "aws_vpc_endpoint" "ecr_dkr" {
  count               = var.enable_network_egress ? 1 : 0
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.ap-northeast-2.ecr.dkr"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [aws_subnet.private_2a.id]
  security_group_ids  = [aws_security_group.endpoint.id]
  private_dns_enabled = true

  tags = {
    Name = "bid-radar-vpce-ecr-dkr"
  }
}

resource "aws_vpc_endpoint" "ssmmessages" {
  count               = var.enable_network_egress ? 1 : 0
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.ap-northeast-2.ssmmessages"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [aws_subnet.private_2a.id]
  security_group_ids  = [aws_security_group.endpoint.id]
  private_dns_enabled = true

  tags = {
    Name = "bid-radar-vpce-ssmmessages"
  }
}

resource "aws_vpc_endpoint" "ec2messages" {
  count               = var.enable_network_egress ? 1 : 0
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.ap-northeast-2.ec2messages"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [aws_subnet.private_2a.id]
  security_group_ids  = [aws_security_group.endpoint.id]
  private_dns_enabled = true

  tags = {
    Name = "bid-radar-vpce-ec2messages"
  }
}
