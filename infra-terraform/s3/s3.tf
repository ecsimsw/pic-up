# S3
resource "aws_s3_bucket" "picup-storage" {
  bucket = "picup-storage"
  tags   = {
    Name      = "picup-storage",
    CreatedAt = "20240422"
  }
}

# s3 policy
data "aws_iam_policy_document" "s3_policy-data" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.picup-storage.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.picup-storage-oai.iam_arn]
    }
  }
}
resource "aws_s3_bucket_policy" "s3_policy" {
  bucket = aws_s3_bucket.picup-storage.id
  policy = data.aws_iam_policy_document.s3_policy-data.json
}

resource "aws_cloudfront_origin_access_identity" "picup-storage-oai" {
  comment = "picup-storage-oai"
}

output "picup-storage-id" {
  value = element(concat(aws_s3_bucket.picup-storage.*.id, [""]), 0)
}

output "picup-storage-arn" {
  value = element(concat(aws_s3_bucket.picup-storage.*.arn, [""]), 0)
}

output "picup-storage-regional-domain-name" {
  value = element(concat(aws_s3_bucket.picup-storage.*.bucket_regional_domain_name, [""]), 0)
}

output "picup-storage-domain-name" {
  value = try(aws_s3_bucket.picup-storage.bucket_domain_name, "")
}