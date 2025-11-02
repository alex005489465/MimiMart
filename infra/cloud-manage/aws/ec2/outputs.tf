# ========================================
# EC2 實例輸出
# ========================================

output "instance_ids" {
  description = "EC2 實例 ID 列表"
  value       = aws_instance.main[*].id
}

output "instance_arns" {
  description = "EC2 實例 ARN 列表"
  value       = aws_instance.main[*].arn
}

output "instance_states" {
  description = "EC2 實例狀態列表"
  value       = aws_instance.main[*].instance_state
}

# ========================================
# 網路資訊
# ========================================

output "private_ips" {
  description = "EC2 實例私有 IP 位址列表"
  value       = aws_instance.main[*].private_ip
}

output "public_ips" {
  description = "EC2 實例公開 IP 位址列表 (若有分配)"
  value       = aws_instance.main[*].public_ip
}

output "private_dns" {
  description = "EC2 實例私有 DNS 名稱列表"
  value       = aws_instance.main[*].private_dns
}

output "public_dns" {
  description = "EC2 實例公開 DNS 名稱列表"
  value       = aws_instance.main[*].public_dns
}

# ========================================
# ENI 資訊
# ========================================

output "eni_ids" {
  description = "網路介面 (ENI) ID 列表"
  value       = aws_network_interface.main[*].id
}

output "eni_private_ips" {
  description = "ENI 私有 IP 位址列表"
  value       = aws_network_interface.main[*].private_ips
}

# ========================================
# 其他資訊
# ========================================

output "availability_zones" {
  description = "EC2 實例所在的可用區列表"
  value       = aws_instance.main[*].availability_zone
}

output "key_name" {
  description = "使用的 SSH Key Pair 名稱"
  value       = var.key_name
}

output "instance_type" {
  description = "EC2 實例類型"
  value       = var.instance_type
}

# ========================================
# EIP 資訊
# ========================================

output "eip_association_ids" {
  description = "Elastic IP 關聯 ID 列表"
  value       = var.associate_eip ? aws_eip_association.main[*].id : []
}

output "eip_public_ips" {
  description = "Elastic IP 公開 IP 位址列表"
  value       = var.associate_eip ? aws_eip_association.main[*].public_ip : []
}

# ========================================
# 連線資訊摘要
# ========================================

output "connection_info" {
  description = "SSH 連線資訊摘要"
  value = [
    for i, instance in aws_instance.main : {
      name        = "${var.project_name}-ec2-${i + 1}"
      instance_id = instance.id
      private_ip  = instance.private_ip
      public_ip   = var.associate_eip ? aws_eip_association.main[i].public_ip : (instance.public_ip != "" ? instance.public_ip : "未分配")
      ssh_command = var.associate_eip ? "ssh -i /path/to/${var.key_name}.pem ec2-user@${aws_eip_association.main[i].public_ip}" : (instance.public_ip != "" ? "ssh -i /path/to/${var.key_name}.pem ec2-user@${instance.public_ip}" : "需要 VPN 或 Bastion: ssh -i /path/to/${var.key_name}.pem ec2-user@${instance.private_ip}")
    }
  ]
}
