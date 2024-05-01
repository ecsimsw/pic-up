## CloudFront distribution
#resource "aws_cloudfront_distribution" "my_distribution" {
#  origin {
#    domain_name = var.s3_regional_domain_name
#    origin_id   = var.s3-storage-id
#
#    s3_origin_config {
#      origin_access_identity = var.s3-storage-oai
#    }
#
#    custom_origin_config {
#      http_port              = 80
#      https_port             = 443
#      origin_protocol_policy = "https-only"
#      origin_ssl_protocols   = ["TLSv1.2"]
#    }
#  }
#
#  default_cache_behavior {
#    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
#    cached_methods   = ["GET", "HEAD", "OPTIONS"]
#    target_origin_id = var.s3-storage-id
#    viewer_protocol_policy = "redirect-to-https"
#    forwarded_values {
#      query_string = false
#      cookies {
#        forward = "none"
#      }
#    }
#    min_ttl             = 0
#    default_ttl         = 3600
#    max_ttl             = 86400
#  }
#  restrictions {
#    geo_restriction {
#      restriction_type = "none"
#    }
#  }
#  viewer_certificate {
#    cloudfront_default_certificate = true
#  }
#  enabled = true
#}

resource "aws_cloudfront_origin_access_control" "cloudfront_s3_oac" {
  name                              = "CloudFront S3 OAC"
  description                       = "Cloud Front S3 OAC"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "my_distrib" {

  origin {
    domain_name = var.s3_regional_domain_name
    origin_id   = ""
    origin_access_control_id = aws_cloudfront_origin_access_control.cloudfront_s3_oac.id
  }

  enabled = true
  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "distribution"

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "allow-all"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
      locations        = []
    }
  }
}
