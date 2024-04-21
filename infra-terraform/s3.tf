# module "s3" {
#   source = "./s3"
# }

# module "cloudfront" {
#   source = "./cloudfront"
# }

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

# CloudFront distribution
resource "aws_cloudfront_distribution" "my_distribution" {
  origin {
    domain_name = aws_s3_bucket.picup-storage.bucket_regional_domain_name
    origin_id   = aws_s3_bucket.picup-storage.id

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.picup-storage-oai.cloudfront_access_identity_path
    }

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "https-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = aws_s3_bucket.picup-storage.id

    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }
    min_ttl             = 0
    default_ttl         = 3600
    max_ttl             = 86400
  }
  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
  viewer_certificate {
    cloudfront_default_certificate = true
  }
  enabled = true
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