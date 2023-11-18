output "vpc_id" {
  value = aws_vpc.myCloudDefaultVpc.id
}

output "public_subnet_id" {
  value = aws_subnet.myCloudPublicSubnet.id
}