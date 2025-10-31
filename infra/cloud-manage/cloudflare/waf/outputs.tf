# ============================================================================
# WAF Ruleset 輸出
# ============================================================================
output "ruleset_id" {
  description = "WAF 規則集 ID"
  value       = cloudflare_ruleset.waf_custom_rules.id
}

output "ruleset_name" {
  description = "WAF 規則集名稱"
  value       = cloudflare_ruleset.waf_custom_rules.name
}

output "rules_count" {
  description = "WAF 規則數量"
  value       = length(var.waf_rules)
}

output "rules_summary" {
  description = "WAF 規則摘要"
  value = {
    for idx, rule in var.waf_rules : idx => {
      action      = rule.action
      description = rule.description
      enabled     = lookup(rule, "enabled", true)
    }
  }
}
