# ============================================================
# SSH Security Group 輸出
# ============================================================

output "ssh_sg_id" {
  description = "SSH Security Group ID"
  value       = aws_security_group.ssh.id
}

output "ssh_sg_name" {
  description = "SSH Security Group 名稱"
  value       = aws_security_group.ssh.name
}

output "ssh_sg_arn" {
  description = "SSH Security Group ARN"
  value       = aws_security_group.ssh.arn
}

# ============================================================
# Web Security Group 輸出 (目前註解)
# ============================================================
#
# ⚠️ 當啟用 web-sg.tf 時,同時取消註解以下輸出 ⚠️
#
# output "web_sg_id" {
#   description = "Web Security Group ID"
#   value       = aws_security_group.web.id
# }
#
# output "web_sg_name" {
#   description = "Web Security Group 名稱"
#   value       = aws_security_group.web.name
# }
#
# output "web_sg_arn" {
#   description = "Web Security Group ARN"
#   value       = aws_security_group.web.arn
# }

# ============================================================
# 所有 Security Groups (便於 EC2 模組引用)
# ============================================================

output "all_sg_ids" {
  description = "所有 Security Group IDs 的列表 (目前僅包含 SSH SG)"
  value = [
    aws_security_group.ssh.id,
    # aws_security_group.web.id,  # 啟用 Web SG 時取消註解
  ]
}

# ============================================================
# IP 範圍資訊 (除錯用)
# ============================================================

output "admin_ip_count" {
  description = "管理員 IP 數量統計"
  value = {
    ipv4_count = length(module.ip_ranges.admin_only_v4)
    ipv6_count = length(module.ip_ranges.admin_only_v6)
    total      = length(module.ip_ranges.admin_only_v4) + length(module.ip_ranges.admin_only_v6)
  }
}

output "cloudflare_ip_count" {
  description = "Cloudflare IP 數量統計"
  value = {
    ipv4_count = length(module.ip_ranges.cloudflare_ipv4)
    ipv6_count = length(module.ip_ranges.cloudflare_ipv6)
    total      = length(module.ip_ranges.cloudflare_ipv4) + length(module.ip_ranges.cloudflare_ipv6)
  }
}
