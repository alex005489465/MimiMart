# ====================================================================
# IP Ranges 共用模組
# ====================================================================
# 用途: 統一管理 Cloudflare IP 和開發者 IP，供所有模組引用
# 使用方式:
#   module "ip_ranges" {
#     source = "../../modules/ip-ranges"
#   }
# ====================================================================

terraform {
  required_version = ">= 1.0"
}

# ====================================================================
# 從 JSON 讀取開發者 IP 配置
# ====================================================================

locals {
  # 讀取 developer-ips.json
  ip_config_file = "${path.module}/developer-ips.json"
  ip_config_exists = fileexists(local.ip_config_file)

  # 直接讀取 JSON 並使用 try() 處理不存在的情況
  # 開發者與辦公室 IP（分 IPv4 和 IPv6）
  dev_ips_v4    = try(jsondecode(file(local.ip_config_file)).developer_ips_v4, [])
  dev_ips_v6    = try(jsondecode(file(local.ip_config_file)).developer_ips_v6, [])
  office_ips_v4 = try(jsondecode(file(local.ip_config_file)).office_ips_v4, [])
  office_ips_v6 = try(jsondecode(file(local.ip_config_file)).office_ips_v6, [])

  # 合併所有開發者和辦公室 IP
  dev_ips_all    = concat(local.dev_ips_v4, local.dev_ips_v6)
  office_ips_all = concat(local.office_ips_v4, local.office_ips_v6)

  # Cloudflare IPs (來自 cloudflare-ips.tf)
  cf_ipv4 = local.cloudflare_ipv4_ranges
  cf_ipv6 = local.cloudflare_ipv6_ranges

  # ====================================================================
  # 常用 IP 組合
  # ====================================================================

  # Cloudflare IPv4 + 開發者 IPv4（用於 S3 Bucket Policy 等僅支援 IPv4 的場景）
  cloudflare_and_developers_v4 = concat(local.cf_ipv4, local.dev_ips_v4)

  # Cloudflare IPv6 + 開發者 IPv6
  cloudflare_and_developers_v6 = concat(local.cf_ipv6, local.dev_ips_v6)

  # Cloudflare + 開發者 IP（全部，IPv4 + IPv6）
  cloudflare_and_developers = concat(
    local.cf_ipv4,
    local.cf_ipv6,
    local.dev_ips_v4,
    local.dev_ips_v6
  )

  # 僅管理員 IP（IPv4 + IPv6）
  admin_only = concat(
    local.dev_ips_v4,
    local.dev_ips_v6,
    local.office_ips_v4,
    local.office_ips_v6
  )

  # 僅管理員 IPv4
  admin_only_v4 = concat(local.dev_ips_v4, local.office_ips_v4)

  # 僅管理員 IPv6
  admin_only_v6 = concat(local.dev_ips_v6, local.office_ips_v6)

  # 所有信任的 IP（IPv4 + IPv6）
  all_trusted_ips = concat(
    local.cf_ipv4,
    local.cf_ipv6,
    local.dev_ips_v4,
    local.dev_ips_v6,
    local.office_ips_v4,
    local.office_ips_v6
  )

  # Cloudflare 全部 IP（IPv4 + IPv6）
  cloudflare_all = concat(local.cf_ipv4, local.cf_ipv6)
}
