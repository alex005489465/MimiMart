# ========================================
# Terraform 和 Provider 配置
# ========================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region     = var.aws_region
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
}

# ========================================
# 資料來源 - 自動查詢最新 Amazon Linux 2023 AMI
# ========================================

data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-arm64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "state"
    values = ["available"]
  }
}

# ========================================
# 網路介面 (ENI)
# ========================================

resource "aws_network_interface" "main" {
  count           = var.instance_count
  subnet_id       = var.subnet_ids[count.index % length(var.subnet_ids)]
  private_ips     = var.private_ips != null ? [var.private_ips[count.index]] : null
  security_groups = var.security_group_ids

  tags = merge(
    {
      Name        = "${var.project_name}-${var.environment}-eni-${count.index + 1}"
      Environment = var.environment
      Project     = var.project_name
    },
    var.additional_tags
  )
}

# ========================================
# Elastic IP 關聯到 ENI
# ========================================

resource "aws_eip_association" "main" {
  count                = var.associate_eip ? var.instance_count : 0
  network_interface_id = aws_network_interface.main[count.index].id
  allocation_id        = var.eip_allocation_ids[count.index]
}

# ========================================
# EC2 實例
# ========================================

resource "aws_instance" "main" {
  count         = var.instance_count
  ami           = var.ami_id != null ? var.ami_id : data.aws_ami.amazon_linux_2023.id
  instance_type = var.instance_type
  key_name      = var.key_name

  # 使用 ENI 進行網路配置
  network_interface {
    network_interface_id = aws_network_interface.main[count.index].id
    device_index         = 0
  }

  # 根磁碟配置
  root_block_device {
    volume_type           = var.root_volume_type
    volume_size           = var.root_volume_size
    delete_on_termination = var.root_volume_delete_on_termination
    encrypted             = var.root_volume_encrypted

    tags = {
      Name        = "${var.project_name}-${var.environment}-root-${count.index + 1}"
      Environment = var.environment
      Project     = var.project_name
    }
  }

  # User Data - 安裝 Docker 和 Docker Compose
  user_data = var.user_data_script != null ? var.user_data_script : file("${path.module}/user-data.sh")

  tags = merge(
    {
      Name        = "${var.project_name}-${var.environment}-instance-${count.index + 1}"
      Environment = var.environment
      Project     = var.project_name
    },
    var.additional_tags
  )
}
