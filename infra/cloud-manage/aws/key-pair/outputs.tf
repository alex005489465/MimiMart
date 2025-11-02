# Key Pair 輸出
output "key_pair_id" {
  description = "Key Pair 資源 ID"
  value       = aws_key_pair.main.id
}

output "key_pair_name" {
  description = "Key Pair 名稱（用於 EC2 實例配置）"
  value       = aws_key_pair.main.key_name
}

output "key_pair_arn" {
  description = "Key Pair ARN"
  value       = aws_key_pair.main.arn
}

output "key_pair_fingerprint" {
  description = "Key Pair 指紋"
  value       = aws_key_pair.main.fingerprint
}
