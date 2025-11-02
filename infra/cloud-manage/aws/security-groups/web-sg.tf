# ============================================================
# Web Security Group (HTTP/HTTPS + Cloudflare)
# ============================================================
#
# ⚠️ 目前未啟用,全部註解 ⚠️
#
# 此檔案包含完整的 Web Security Group 配置,供未來使用。
# 當需要將流量從 Cloudflare 轉發到 EC2 時,可以取消註解啟用。
#
# 功能說明:
# - 開放 HTTP (80) 和 HTTPS (443) 端口
# - 僅允許來自 Cloudflare IP 範圍的流量
# - Cloudflare IP 自動從 modules/ip-ranges 模組取得
#   - IPv4: 15 個 CIDR 範圍
#   - IPv6: 7 個 CIDR 範圍
#
# 如何啟用此 Security Group:
# 1. 移除本檔案中所有註解符號 (#)
# 2. 在 outputs.tf 取消註解 web_sg 相關輸出
# 3. 執行以下指令:
#    cd d:\Projects\MimiMart\infra\cloud-manage
#    docker-compose --env-file aws/.env run --rm terraform "cd security-groups && terraform plan"
#    docker-compose --env-file aws/.env run --rm terraform "cd security-groups && terraform apply"
#
# 安全性說明:
# - 僅允許 Cloudflare CDN IP,阻擋直接存取
# - 建議搭配 Cloudflare WAF 使用
# - Cloudflare IP 範圍定期更新 (每季檢查一次)
# ============================================================

# # ============================================================
# # Web Security Group 資源定義
# # ============================================================
#
# resource "aws_security_group" "web" {
#   name        = "${var.project_name}-${var.environment}-web-sg"
#   description = "Allow HTTP/HTTPS traffic from Cloudflare"
#   vpc_id      = var.vpc_id
#
#   tags = merge(
#     local.common_tags,
#     {
#       Name        = "${var.project_name}-${var.environment}-web-sg"
#       Description = "Web 流量 (Cloudflare CDN)"
#     }
#   )
# }
#
# # ============================================================
# # HTTP (80) 入站規則 - IPv4
# # ============================================================
#
# resource "aws_security_group_rule" "web_http_ingress_v4" {
#   type              = "ingress"
#   from_port         = 80
#   to_port           = 80
#   protocol          = "tcp"
#   cidr_blocks       = module.ip_ranges.cloudflare_ipv4
#   security_group_id = aws_security_group.web.id
#   description       = "HTTP from Cloudflare IPv4"
# }
#
# # ============================================================
# # HTTP (80) 入站規則 - IPv6
# # ============================================================
#
# resource "aws_security_group_rule" "web_http_ingress_v6" {
#   type              = "ingress"
#   from_port         = 80
#   to_port           = 80
#   protocol          = "tcp"
#   ipv6_cidr_blocks  = module.ip_ranges.cloudflare_ipv6
#   security_group_id = aws_security_group.web.id
#   description       = "HTTP from Cloudflare IPv6"
# }
#
# # ============================================================
# # HTTPS (443) 入站規則 - IPv4
# # ============================================================
#
# resource "aws_security_group_rule" "web_https_ingress_v4" {
#   type              = "ingress"
#   from_port         = 443
#   to_port           = 443
#   protocol          = "tcp"
#   cidr_blocks       = module.ip_ranges.cloudflare_ipv4
#   security_group_id = aws_security_group.web.id
#   description       = "HTTPS from Cloudflare IPv4"
# }
#
# # ============================================================
# # HTTPS (443) 入站規則 - IPv6
# # ============================================================
#
# resource "aws_security_group_rule" "web_https_ingress_v6" {
#   type              = "ingress"
#   from_port         = 443
#   to_port           = 443
#   protocol          = "tcp"
#   ipv6_cidr_blocks  = module.ip_ranges.cloudflare_ipv6
#   security_group_id = aws_security_group.web.id
#   description       = "HTTPS from Cloudflare IPv6"
# }
#
# # ============================================================
# # Web 出站規則 - 允許所有流量
# # ============================================================
#
# resource "aws_security_group_rule" "web_egress" {
#   type              = "egress"
#   from_port         = 0
#   to_port           = 0
#   protocol          = "-1"
#   cidr_blocks       = ["0.0.0.0/0"]
#   ipv6_cidr_blocks  = ["::/0"]
#   security_group_id = aws_security_group.web.id
#   description       = "Allow all outbound traffic"
# }
