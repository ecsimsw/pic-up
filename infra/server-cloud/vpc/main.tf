resource "aws_vpc" "myCloudDefaultVpc" {
  cidr_block = "172.52.0.0/16"
  tags = {
    Name = "myCloud_default_vpc"
  }
}

resource "aws_internet_gateway" "myCloudIGW" {
  vpc_id = aws_vpc.myCloudDefaultVpc.id

  tags = {
    Name = "myCloud-igw"
  }
}

resource "aws_route_table" "myCloudRT" {
  vpc_id = aws_vpc.myCloudDefaultVpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.myCloudIGW.id
  }

  tags = {
    Name = "myCloud-rt"
  }
}

resource "aws_subnet" "myCloudPublicSubnet" {
  vpc_id     = aws_vpc.myCloudDefaultVpc.id
  cidr_block = "172.52.1.0/24"
  map_public_ip_on_launch = true
  tags = {
    Name = "myCloud_public_subnet"
  }
}

resource "aws_route_table_association" "cicd_rta" {
  subnet_id      = aws_subnet.myCloudPublicSubnet.id
  route_table_id = aws_route_table.myCloudRT.id
}
