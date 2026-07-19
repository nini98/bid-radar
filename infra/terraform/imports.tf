# 콘솔에서 수동으로 만든 기존 리소스를 Terraform으로 가져오기 위한 import 블록.
# 대응하는 리소스 설정은 generated.tf에 있다 (provider의 config 자동생성 버그로 일부는 수동 수정함).
#
# NAT Gateway / EIP / Interface VPC Endpoint 7개는 비용 절감을 위해 삭제 대상이라
# generated.tf에서 리소스 블록을 제거했고, 그에 맞춰 여기 import 블록도 제거했다.

import {
  to = aws_vpc.main
  id = "vpc-0e974aca7cbb733c4"
}

import {
  to = aws_subnet.public_2a
  id = "subnet-03d76dfbfc4293b2f"
}

import {
  to = aws_subnet.public_2c
  id = "subnet-084682701469e1edf"
}

import {
  to = aws_subnet.private_2a
  id = "subnet-00b4479f4820b60fd"
}

import {
  to = aws_subnet.private_2c
  id = "subnet-0f5f1fa8ad0cc2412"
}

import {
  to = aws_internet_gateway.main
  id = "igw-0c506d60ad2677995"
}

import {
  to = aws_route_table.public
  id = "rtb-03116ed6b32b67a02"
}

import {
  to = aws_route_table.private
  id = "rtb-07656b03152081aa7"
}

import {
  to = aws_security_group.alb
  id = "sg-087ea442fcc4180a6"
}

import {
  to = aws_security_group.ec2
  id = "sg-00ff836a761760f14"
}

import {
  to = aws_security_group.endpoint
  id = "sg-03d97bdadd6039fc3"
}

import {
  to = aws_vpc_endpoint.s3
  id = "vpce-075b96c8801787d9e"
}

import {
  to = aws_iam_role.ec2
  id = "bid-radar-ec2-role"
}

import {
  to = aws_iam_instance_profile.ec2
  id = "bid-radar-ec2-role"
}

import {
  to = aws_iam_role_policy_attachment.ec2_ecr_readonly
  id = "bid-radar-ec2-role/arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

import {
  to = aws_iam_role_policy_attachment.ec2_ssm
  id = "bid-radar-ec2-role/arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

import {
  to = aws_iam_role_policy_attachment.ec2_cloudwatch
  id = "bid-radar-ec2-role/arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

import {
  to = aws_iam_role_policy_attachment.ec2_secrets
  id = "bid-radar-ec2-role/arn:aws:iam::aws:policy/SecretsManagerReadWrite"
}

import {
  to = aws_route_table_association.public_2a
  id = "subnet-03d76dfbfc4293b2f/rtb-03116ed6b32b67a02"
}

import {
  to = aws_route_table_association.public_2c
  id = "subnet-084682701469e1edf/rtb-03116ed6b32b67a02"
}

import {
  to = aws_route_table_association.private_2a
  id = "subnet-00b4479f4820b60fd/rtb-07656b03152081aa7"
}

import {
  to = aws_route_table_association.private_2c
  id = "subnet-0f5f1fa8ad0cc2412/rtb-07656b03152081aa7"
}

import {
  to = aws_instance.main
  id = "i-0c4cb277e65bbf075"
}

import {
  to = aws_ecr_repository.backend
  id = "bid-radar/backend"
}

import {
  to = aws_ecr_repository.frontend
  id = "bid-radar/frontend"
}

import {
  to = aws_ecr_repository.ai_worker
  id = "bid-radar/ai-worker"
}
