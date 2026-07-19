# __generated__ by Terraform
# Please review these resources and move them into your main configuration files.

# __generated__ by Terraform from "igw-0c506d60ad2677995"
resource "aws_internet_gateway" "main" {
  tags = {
    Name = "bid-radar-igw"
  }
  tags_all = {
    Name = "bid-radar-igw"
  }
  vpc_id = aws_vpc.main.id
}

# __generated__ by Terraform
resource "aws_route_table" "public" {
  propagating_vgws = []
  route = [{
    carrier_gateway_id         = ""
    cidr_block                 = "0.0.0.0/0"
    core_network_arn           = ""
    destination_prefix_list_id = ""
    egress_only_gateway_id     = ""
    gateway_id                 = aws_internet_gateway.main.id
    ipv6_cidr_block            = null
    local_gateway_id           = ""
    nat_gateway_id             = ""
    network_interface_id       = ""
    transit_gateway_id         = ""
    vpc_endpoint_id            = ""
    vpc_peering_connection_id  = ""
  }]
  tags = {
    Name = "bid-radar-rt-public"
  }
  tags_all = {
    Name = "bid-radar-rt-public"
  }
  vpc_id = aws_vpc.main.id
}

# __generated__ by Terraform
resource "aws_route_table" "private" {
  propagating_vgws = []
  route = var.enable_network_egress ? [{
    carrier_gateway_id         = ""
    cidr_block                 = "0.0.0.0/0"
    core_network_arn           = ""
    destination_prefix_list_id = ""
    egress_only_gateway_id     = ""
    gateway_id                 = ""
    ipv6_cidr_block            = null
    local_gateway_id           = ""
    nat_gateway_id             = aws_nat_gateway.main[0].id
    network_interface_id       = ""
    transit_gateway_id         = ""
    vpc_endpoint_id            = ""
    vpc_peering_connection_id  = ""
  }] : []
  tags = {
    Name = "bid-radar-rt-private"
  }
  tags_all = {
    Name = "bid-radar-rt-private"
  }
  vpc_id = aws_vpc.main.id
}

# __generated__ by Terraform
resource "aws_subnet" "public_2a" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "ap-northeast-2a"
  cidr_block                                     = "10.0.1.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = false
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "bid-radar-public-2a"
  }
  tags_all = {
    Name = "bid-radar-public-2a"
  }
  vpc_id = aws_vpc.main.id
}

# __generated__ by Terraform from "sg-00ff836a761760f14"
resource "aws_security_group" "ec2" {
  description = "Allow inbound traffic from ALB to EC2"
  egress = [{
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 0
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "-1"
    security_groups  = []
    self             = false
    to_port          = 0
  }]
  ingress = [{
    cidr_blocks      = []
    description      = ""
    from_port        = 8080
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = [aws_security_group.alb.id]
    self             = false
    to_port          = 8080
    }, {
    cidr_blocks      = []
    description      = ""
    from_port        = 80
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = [aws_security_group.alb.id]
    self             = false
    to_port          = 80
  }]
  name                   = "bid-radar-sg-ec2"
  name_prefix            = null
  revoke_rules_on_delete = null
  tags                   = {}
  tags_all               = {}
  vpc_id                 = aws_vpc.main.id
}

# __generated__ by Terraform
resource "aws_subnet" "private_2a" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "ap-northeast-2a"
  cidr_block                                     = "10.0.11.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = false
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "bid-radar-private-2a"
  }
  tags_all = {
    Name = "bid-radar-private-2a"
  }
  vpc_id = aws_vpc.main.id
}

# __generated__ by Terraform from "sg-087ea442fcc4180a6"
resource "aws_security_group" "alb" {
  description = "Allow inbound traffic from internet to ALB"
  egress = [{
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 0
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "-1"
    security_groups  = []
    self             = false
    to_port          = 0
  }]
  ingress = [{
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 443
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = []
    self             = false
    to_port          = 443
  }]
  name                   = "bid-radar-sg-alb"
  name_prefix            = null
  revoke_rules_on_delete = null
  tags                   = {}
  tags_all               = {}
  vpc_id                 = aws_vpc.main.id
}

# __generated__ by Terraform from "vpce-075b96c8801787d9e"
resource "aws_vpc_endpoint" "s3" {
  auto_accept     = null
  ip_address_type = "ipv4"
  policy = jsonencode({
    Statement = [{
      Action    = "*"
      Effect    = "Allow"
      Principal = "*"
      Resource  = "*"
    }]
    Version = "2008-10-17"
  })
  private_dns_enabled        = false
  resource_configuration_arn = null
  route_table_ids            = [aws_route_table.private.id]
  security_group_ids         = []
  service_name               = "com.amazonaws.ap-northeast-2.s3"
  service_network_arn        = null
  service_region             = "ap-northeast-2"
  subnet_ids                 = []
  tags = {
    Name = "bid-radar-vpce-s3"
  }
  tags_all = {
    Name = "bid-radar-vpce-s3"
  }
  vpc_endpoint_type = "Gateway"
  vpc_id            = aws_vpc.main.id
  dns_options {
    dns_record_ip_type                             = "service-defined"
    private_dns_only_for_inbound_resolver_endpoint = false
  }
}

# __generated__ by Terraform
resource "aws_subnet" "private_2c" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "ap-northeast-2c"
  cidr_block                                     = "10.0.12.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = false
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "bid-radar-private-2c"
  }
  tags_all = {
    Name = "bid-radar-private-2c"
  }
  vpc_id = aws_vpc.main.id
}

# __generated__ by Terraform
resource "aws_subnet" "public_2c" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "ap-northeast-2c"
  cidr_block                                     = "10.0.2.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = false
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "bid-radar-public-2c"
  }
  tags_all = {
    Name = "bid-radar-public-2c"
  }
  vpc_id = aws_vpc.main.id
}

# __generated__ by Terraform from "sg-03d97bdadd6039fc3"
resource "aws_security_group" "endpoint" {
  description = "Allow inbound traffic from EC2 to VPC endpoints"
  egress = [{
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 0
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "-1"
    security_groups  = []
    self             = false
    to_port          = 0
  }]
  ingress = [{
    cidr_blocks      = []
    description      = ""
    from_port        = 443
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = [aws_security_group.ec2.id]
    self             = false
    to_port          = 443
  }]
  name                   = "bid-radar-sg-endpoint"
  name_prefix            = null
  revoke_rules_on_delete = null
  tags                   = {}
  tags_all               = {}
  vpc_id                 = aws_vpc.main.id
}

# __generated__ by Terraform
resource "aws_vpc" "main" {
  assign_generated_ipv6_cidr_block     = false
  cidr_block                           = "10.0.0.0/16"
  enable_dns_hostnames                 = true
  enable_dns_support                   = true
  enable_network_address_usage_metrics = false
  instance_tenancy                     = "default"
  ipv4_ipam_pool_id                    = null
  ipv4_netmask_length                  = null
  ipv6_cidr_block                      = null
  ipv6_cidr_block_network_border_group = null
  ipv6_ipam_pool_id                    = null
  ipv6_netmask_length                  = null
  tags = {
    Name = "bid-radar-vpc"
  }
  tags_all = {
    Name = "bid-radar-vpc"
  }
}

# __generated__ by Terraform
resource "aws_instance" "main" {
  ami                                  = "ami-0436b3a61a7a7e22a"
  associate_public_ip_address          = false
  availability_zone                    = "ap-northeast-2a"
  disable_api_stop                     = false
  disable_api_termination              = false
  ebs_optimized                        = true
  enable_primary_ipv6                  = null
  get_password_data                    = false
  hibernation                          = false
  host_id                              = null
  host_resource_group_arn              = null
  iam_instance_profile                 = aws_iam_instance_profile.ec2.name
  instance_initiated_shutdown_behavior = "stop"
  instance_type                        = "t3.small"
  key_name                             = var.ec2_key_name
  monitoring                           = false
  placement_group                      = null
  placement_partition_number           = 0
  private_ip                           = "10.0.11.237"
  secondary_private_ips                = []
  security_groups                      = []
  source_dest_check                    = true
  subnet_id                            = aws_subnet.private_2a.id
  tags = {
    Name = "bid-radar-ec2"
  }
  tags_all = {
    Name = "bid-radar-ec2"
  }
  tenancy                     = "default"
  user_data                   = null
  user_data_base64            = null
  user_data_replace_on_change = null
  volume_tags                 = null
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  capacity_reservation_specification {
    capacity_reservation_preference = "open"
  }
  cpu_options {
    amd_sev_snp      = null
    core_count       = 1
    threads_per_core = 2
  }
  credit_specification {
    cpu_credits = "unlimited"
  }
  enclave_options {
    enabled = false
  }
  maintenance_options {
    auto_recovery = "default"
  }
  metadata_options {
    http_endpoint               = "enabled"
    http_protocol_ipv6          = "disabled"
    http_put_response_hop_limit = 2
    http_tokens                 = "required"
    instance_metadata_tags      = "disabled"
  }
  private_dns_name_options {
    enable_resource_name_dns_a_record    = false
    enable_resource_name_dns_aaaa_record = false
    hostname_type                        = "ip-name"
  }
  root_block_device {
    delete_on_termination = true
    encrypted             = false
    iops                  = 3000
    kms_key_id            = null
    tags                  = {}
    tags_all              = {}
    throughput            = 125
    volume_size           = 20
    volume_type           = "gp3"
  }
}
# __generated__ by Terraform
# Please review these resources and move them into your main configuration files.

# __generated__ by Terraform from "bid-radar/ai-worker"
resource "aws_ecr_repository" "ai_worker" {
  force_delete         = null
  image_tag_mutability = "MUTABLE"
  name                 = "bid-radar/ai-worker"
  tags                 = {}
  tags_all             = {}
  encryption_configuration {
    encryption_type = "AES256"
    kms_key         = null
  }
  image_scanning_configuration {
    scan_on_push = false
  }
}

# __generated__ by Terraform from "bid-radar/frontend"
resource "aws_ecr_repository" "frontend" {
  force_delete         = null
  image_tag_mutability = "MUTABLE"
  name                 = "bid-radar/frontend"
  tags                 = {}
  tags_all             = {}
  encryption_configuration {
    encryption_type = "AES256"
    kms_key         = null
  }
  image_scanning_configuration {
    scan_on_push = false
  }
}

# __generated__ by Terraform from "bid-radar/backend"
resource "aws_ecr_repository" "backend" {
  force_delete         = null
  image_tag_mutability = "MUTABLE"
  name                 = "bid-radar/backend"
  tags                 = {}
  tags_all             = {}
  encryption_configuration {
    encryption_type = "AES256"
    kms_key         = null
  }
  image_scanning_configuration {
    scan_on_push = false
  }
}

# __generated__ by Terraform from "bid-radar-ec2-role"
resource "aws_iam_role" "ec2" {
  assume_role_policy = jsonencode({
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
    Version = "2012-10-17"
  })
  description           = "Allows EC2 instances to call AWS services on your behalf."
  force_detach_policies = false
  max_session_duration  = 3600
  name                  = "bid-radar-ec2-role"
  name_prefix           = null
  path                  = "/"
  permissions_boundary  = null
  tags                  = {}
  tags_all              = {}
}

resource "aws_iam_instance_profile" "ec2" {
  name = "bid-radar-ec2-role"
  path = "/"
  role = aws_iam_role.ec2.name
}

resource "aws_iam_role_policy_attachment" "ec2_ecr_readonly" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_cloudwatch" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_role_policy_attachment" "ec2_secrets" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
}

resource "aws_route_table_association" "public_2a" {
  subnet_id      = aws_subnet.public_2a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_2c" {
  subnet_id      = aws_subnet.public_2c.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private_2a" {
  subnet_id      = aws_subnet.private_2a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_2c" {
  subnet_id      = aws_subnet.private_2c.id
  route_table_id = aws_route_table.private.id
}
