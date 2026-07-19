# EC2 SSH 키페어는 Terraform으로 관리하지 않는다. aws_key_pair 리소스로 import를
# 시도하면 public_key가 Required이면서 Computed가 아니라 import 시 state에 채워지지
# 않아, 실제 값과 동일한 public_key를 선언해도 매번 강제 재생성(destroy+create)이
# 계산된다. 기존 키를 불필요하게 재생성하는 위험을 피하기 위해 이름만 변수로 받고,
# 실제 키페어는 아래 방법으로 AWS에 미리 존재해야 하는 외부 전제조건으로 둔다.
#
# 신규 환경에서 준비하는 방법: `aws ec2 create-key-pair --key-name <이름>` 또는
# `aws ec2 import-key-pair --key-name <이름> --public-key-material fileb://<공개키파일>`
variable "ec2_key_name" {
  description = "EC2 인스턴스에 연결할 기존 SSH 키페어 이름 (AWS에 사전 존재해야 함, Terraform이 생성하지 않음)"
  type        = string
  default     = "jay-ubuntu-1"
}
