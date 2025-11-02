# ========================================
# Elastic IP 輸出
# ========================================

output "eip_ids" {
  description = "Elastic IP 資源 ID 列表"
  value       = aws_eip.main[*].id
}

output "eip_allocation_ids" {
  description = "Elastic IP Allocation ID 列表 (供 EC2 模組使用)"
  value       = aws_eip.main[*].allocation_id
}

output "eip_public_ips" {
  description = "Elastic IP 公開 IP 位址列表"
  value       = aws_eip.main[*].public_ip
}

output "eip_public_dns" {
  description = "Elastic IP 公開 DNS 列表"
  value       = aws_eip.main[*].public_dns
}

output "eip_domain" {
  description = "Elastic IP 網域類型"
  value       = var.eip_count > 0 ? aws_eip.main[0].domain : null
}

# ========================================
# 摘要資訊
# ========================================

output "eip_summary" {
  description = "Elastic IP 摘要資訊"
  value = [
    for i, eip in aws_eip.main : {
      name          = "${var.project_name}-${var.environment}-eip-${i + 1}"
      id            = eip.id
      allocation_id = eip.allocation_id
      public_ip     = eip.public_ip
      public_dns    = eip.public_dns
    }
  ]
}
