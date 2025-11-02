# ============================================================
# SSH Security Group
# ============================================================
#
# 此 Security Group 僅允許管理員透過 SSH (Port 22) 連線到 EC2 實例
# 管理員 IP 清單由 modules/ip-ranges 模組管理
#
# 如何更新管理員 IP:
# 1. 編輯 modules/ip-ranges/developer-ips.json
# 2. 重新執行 terraform apply
# ============================================================

resource "aws_security_group" "ssh" {
  name        = "${var.project_name}-${var.environment}-ssh-sg"
  description = "Allow SSH access for administrators"
  vpc_id      = var.vpc_id

  tags = merge(
    local.common_tags,
    {
      Name        = "${var.project_name}-${var.environment}-ssh-sg"
      Description = "SSH 管理員存取"
    }
  )
}

# ============================================================
# SSH 入站規則 - IPv4
# ============================================================

resource "aws_security_group_rule" "ssh_ingress_v4" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = module.ip_ranges.admin_only_v4
  security_group_id = aws_security_group.ssh.id
  description       = "SSH from admin IPv4"
}

# ============================================================
# SSH 入站規則 - IPv6
# ============================================================

resource "aws_security_group_rule" "ssh_ingress_v6" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  ipv6_cidr_blocks  = module.ip_ranges.admin_only_v6
  security_group_id = aws_security_group.ssh.id
  description       = "SSH from admin IPv6"
}

# ============================================================
# SSH 出站規則 - 允許所有流量
# ============================================================

resource "aws_security_group_rule" "ssh_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  ipv6_cidr_blocks  = ["::/0"]
  security_group_id = aws_security_group.ssh.id
  description       = "Allow all outbound traffic"
}
