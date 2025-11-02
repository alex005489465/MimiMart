# VPC 輸出
output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "VPC CIDR 區塊"
  value       = aws_vpc.main.cidr_block
}

# Internet Gateway 輸出
output "internet_gateway_id" {
  description = "Internet Gateway ID"
  value       = aws_internet_gateway.main.id
}

# Public Subnet 輸出
output "public_subnet_id" {
  description = "Public Subnet ID"
  value       = aws_subnet.public.id
}

output "public_subnet_cidr" {
  description = "Public Subnet CIDR 區塊"
  value       = aws_subnet.public.cidr_block
}

output "public_subnet_az" {
  description = "Public Subnet 可用區"
  value       = aws_subnet.public.availability_zone
}

# Route Table 輸出
output "public_route_table_id" {
  description = "Public Route Table ID"
  value       = aws_route_table.public.id
}

# 區域資訊
output "region" {
  description = "AWS Region"
  value       = var.aws_region
}
