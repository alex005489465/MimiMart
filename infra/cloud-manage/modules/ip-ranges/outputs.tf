# ====================================================================
# IP Ranges 模組輸出
# ====================================================================

# --------------------------------------------------------------------
# Cloudflare IP 輸出
# --------------------------------------------------------------------

output "cloudflare_ipv4" {
  description = "Cloudflare IPv4 address ranges"
  value       = local.cf_ipv4
}

output "cloudflare_ipv6" {
  description = "Cloudflare IPv6 address ranges"
  value       = local.cf_ipv6
}

output "cloudflare_all" {
  description = "All Cloudflare IP ranges (IPv4 + IPv6)"
  value       = local.cloudflare_all
}

# --------------------------------------------------------------------
# 開發者/管理員 IP 輸出
# --------------------------------------------------------------------

output "developer_ips_v4" {
  description = "Developer IPv4 addresses"
  value       = local.dev_ips_v4
}

output "developer_ips_v6" {
  description = "Developer IPv6 addresses"
  value       = local.dev_ips_v6
}

output "developer_ips" {
  description = "All developer IP addresses (IPv4 + IPv6)"
  value       = local.dev_ips_all
}

output "office_ips_v4" {
  description = "Office IPv4 addresses"
  value       = local.office_ips_v4
}

output "office_ips_v6" {
  description = "Office IPv6 addresses"
  value       = local.office_ips_v6
}

output "office_ips" {
  description = "All office IP addresses (IPv4 + IPv6)"
  value       = local.office_ips_all
}

# --------------------------------------------------------------------
# 常用組合輸出
# --------------------------------------------------------------------

output "cloudflare_and_developers_v4" {
  description = "Cloudflare IPv4 + Developer IPv4 (for S3 Bucket Policy)"
  value       = local.cloudflare_and_developers_v4
}

output "cloudflare_and_developers_v6" {
  description = "Cloudflare IPv6 + Developer IPv6"
  value       = local.cloudflare_and_developers_v6
}

output "cloudflare_and_developers" {
  description = "Cloudflare + Developer IPs (all, IPv4 + IPv6)"
  value       = local.cloudflare_and_developers
}

output "admin_only_v4" {
  description = "Only developer and office IPv4 (for admin endpoints)"
  value       = local.admin_only_v4
}

output "admin_only_v6" {
  description = "Only developer and office IPv6 (for admin endpoints)"
  value       = local.admin_only_v6
}

output "admin_only" {
  description = "Only developer and office IPs (all, IPv4 + IPv6)"
  value       = local.admin_only
}

output "all_trusted_ips" {
  description = "All trusted IPs (Cloudflare + Developer + Office, IPv4 + IPv6)"
  value       = local.all_trusted_ips
}

# --------------------------------------------------------------------
# 元資訊
# --------------------------------------------------------------------

output "config_file_exists" {
  description = "Whether developer-ips.json file exists"
  value       = local.ip_config_exists
}

output "total_ip_count" {
  description = "Total number of IP ranges configured"
  value = {
    cloudflare_ipv4 = length(local.cf_ipv4)
    cloudflare_ipv6 = length(local.cf_ipv6)
    developer_ipv4  = length(local.dev_ips_v4)
    developer_ipv6  = length(local.dev_ips_v6)
    office_ipv4     = length(local.office_ips_v4)
    office_ipv6     = length(local.office_ips_v6)
  }
}
