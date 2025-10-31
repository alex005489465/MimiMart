# ============================================================================
# DNS 記錄輸出
# ============================================================================

output "dns_records" {
  description = "所有已創建的 DNS 記錄資訊"
  value = {
    for name, record in cloudflare_record.records : name => {
      id       = record.id
      hostname = record.hostname
      type     = record.type
      content  = record.content
      proxied  = record.proxied
      ttl      = record.ttl
    }
  }
}

output "dns_record_ids" {
  description = "DNS 記錄 ID 列表"
  value       = { for name, record in cloudflare_record.records : name => record.id }
}

output "dns_record_hostnames" {
  description = "完整的主機名稱列表"
  value       = { for name, record in cloudflare_record.records : name => record.hostname }
}
