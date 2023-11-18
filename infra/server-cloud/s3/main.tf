resource "aws_s3_bucket" "myCloud_S3" {
  bucket = "mycloud-main-s3"
  tags = {
    Name = "mycloud-main-S3",
    CreatedAt = "20231116"
  }
}
