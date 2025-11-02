# ========================================
# AWS 認證資訊
# ========================================

variable "aws_region" {
  description = "AWS 區域"
  type        = string

  validation {
    condition     = can(regex("^[a-z]{2}-[a-z]+-[0-9]$", var.aws_region))
    error_message = "AWS 區域格式不正確，例如：us-east-1、ap-northeast-1"
  }
}

variable "aws_access_key" {
  description = "AWS Access Key ID"
  type        = string
  sensitive   = true
}

variable "aws_secret_key" {
  description = "AWS Secret Access Key"
  type        = string
  sensitive   = true
}

# ========================================
# 專案資訊
# ========================================

variable "project_name" {
  description = "專案名稱，用於資源命名"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.project_name))
    error_message = "專案名稱只能包含小寫字母、數字和連字號"
  }
}

variable "environment" {
  description = "環境名稱 (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "環境名稱必須是 dev, staging 或 prod"
  }
}

# ========================================
# EC2 實例配置
# ========================================

variable "instance_count" {
  description = "要建立的 EC2 實例數量"
  type        = number

  validation {
    condition     = var.instance_count > 0 && var.instance_count <= 10
    error_message = "實例數量必須在 1 到 10 之間"
  }
}

variable "ami_id" {
  description = "AMI ID (Amazon Machine Image) - 若不指定則自動查詢最新的 Amazon Linux 2023 ARM64 AMI"
  type        = string
  default     = null

  validation {
    condition     = var.ami_id == null || can(regex("^ami-[a-z0-9]{8,}$", var.ami_id))
    error_message = "AMI ID 格式不正確，必須以 'ami-' 開頭"
  }
}

variable "instance_type" {
  description = "EC2 實例類型 (例如：t3.micro, t3.small, t4g.micro)"
  type        = string

  validation {
    condition     = can(regex("^[a-z][0-9][a-z]?\\.(nano|micro|small|medium|large|xlarge|[0-9]+xlarge)$", var.instance_type))
    error_message = "實例類型格式不正確，例如：t3.micro, t3.small"
  }
}

# ========================================
# 網路配置
# ========================================

variable "subnet_ids" {
  description = "子網路 ID 列表 (從 VPC 模組的 public_subnet_id 取得)"
  type        = list(string)

  validation {
    condition     = length(var.subnet_ids) > 0
    error_message = "至少需要提供一個子網路 ID"
  }
}

variable "security_group_ids" {
  description = "安全組 ID 列表 (從 Security Groups 模組的 all_sg_ids 取得)"
  type        = list(string)

  validation {
    condition     = length(var.security_group_ids) > 0
    error_message = "至少需要提供一個安全組 ID"
  }
}

variable "private_ips" {
  description = "固定私有 IP 列表 (選用，若不指定則由 AWS 自動分配)"
  type        = list(string)
  default     = null

  validation {
    condition = var.private_ips == null || (
      length(var.private_ips) > 0 &&
      alltrue([for ip in var.private_ips : can(regex("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", ip))])
    )
    error_message = "私有 IP 格式不正確，必須是有效的 IPv4 位址"
  }
}

# ========================================
# SSH 金鑰配置
# ========================================

variable "key_name" {
  description = "SSH Key Pair 名稱 (從 Key Pair 模組的 key_pair_name 取得)"
  type        = string
}

# ========================================
# 儲存配置
# ========================================

variable "root_volume_type" {
  description = "根磁碟類型 (gp3, gp2, io1, io2)"
  type        = string

  validation {
    condition     = contains(["gp3", "gp2", "io1", "io2"], var.root_volume_type)
    error_message = "根磁碟類型必須是 gp3, gp2, io1 或 io2"
  }
}

variable "root_volume_size" {
  description = "根磁碟大小 (GB)"
  type        = number

  validation {
    condition     = var.root_volume_size >= 8 && var.root_volume_size <= 16384
    error_message = "根磁碟大小必須在 8GB 到 16384GB 之間"
  }
}

variable "root_volume_delete_on_termination" {
  description = "終止實例時是否刪除根磁碟"
  type        = bool
  default     = true
}

variable "root_volume_encrypted" {
  description = "是否加密根磁碟"
  type        = bool
  default     = true
}

# ========================================
# 選用配置
# ========================================

variable "user_data_script" {
  description = "自訂使用者資料腳本 (若不指定則使用預設的 Docker 安裝腳本)"
  type        = string
  default     = null
}

# ========================================
# Elastic IP 配置
# ========================================

variable "associate_eip" {
  description = "是否關聯 Elastic IP"
  type        = bool
  default     = false
}

variable "eip_allocation_ids" {
  description = "Elastic IP Allocation ID 列表 (從 EIP 模組的 eip_allocation_ids 輸出取得)"
  type        = list(string)
  default     = []

  validation {
    condition = length(var.eip_allocation_ids) == 0 || (
      alltrue([for id in var.eip_allocation_ids : can(regex("^eipalloc-[a-z0-9]+$", id))])
    )
    error_message = "EIP Allocation ID 格式不正確，必須以 'eipalloc-' 開頭"
  }
}

# ========================================
# 額外標籤
# ========================================

variable "additional_tags" {
  description = "額外的標籤"
  type        = map(string)
  default     = {}
}
