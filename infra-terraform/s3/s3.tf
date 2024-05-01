## S3
#resource "aws_s3_bucket" "picup-storage" {
#  bucket = "picup-storage"
#  tags   = {
#    Name      = "picup-storage",
#    CreatedAt = "20240422"
#  }
#}
#
## s3 policy
#data "aws_iam_policy_document" "s3_policy-data" {
#  statement {
#    actions   = ["s3:GetObject"]
#    resources = ["${aws_s3_bucket.picup-storage.arn}/*"]
#
#    principals {
#      type        = "AWS"
#      identifiers = [aws_cloudfront_origin_access_identity.picup-storage-oai.iam_arn]
#    }
#  }
#}
#resource "aws_s3_bucket_policy" "s3_policy" {
#  bucket = aws_s3_bucket.picup-storage.id
#  policy = data.aws_iam_policy_document.s3_policy-data.json
#}

resource "aws_s3_bucket" "s3_bucket" {
  bucket = var.name
}

resource "aws_s3_bucket_acl" "s3_bucket_acl" {
  bucket = aws_s3_bucket.s3_bucket.id
  acl    = "private"
}

resource "aws_s3_bucket_public_access_block" "block_public_access" {
  bucket = aws_s3_bucket.s3_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
