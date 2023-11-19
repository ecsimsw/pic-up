resource "aws_key_pair" "ssh_key" {
  key_name   = "ssh_key"
  public_key = file("~/.ssh/id_rsa.pub")
}

resource "aws_instance" "myCloudEc2" {
  ami           = "ami-086cae3329a3f7d75"
  instance_type = "t3.medium"
  key_name = aws_key_pair.ssh_key.key_name
  vpc_security_group_ids = [var.security_group_id]
  subnet_id     = var.public_subnet_id
  tags = {
    Name = "myCloud",
    CreatedAt = "20231116"
  }
}
