module "s3" {
 source = "./s3"
 name = "picup-storage"
}

module "cloudfront" {
 source = "./cloudfront"
 depends_on = [module.s3]

 s3_regional_domain_name = module.s3.bucket_regional_domain_name
}

module "cdn-oai" {
 source = "./cdn-oai"
 depends_on = [module.cloudfront]

 s3-storage-id = module.s3.bucket_id
 cloudfront-arn = module.cloudfront.cloudfront_arn
}