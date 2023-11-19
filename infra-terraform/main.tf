module "vpc" {
  source = "./vpc"
}

module "security_group" {
  source = "./sg"
  vpc_id = module.vpc.vpc_id
}

module "ec2" {
  source = "./ec2"
  vpc_id = module.vpc.vpc_id
  security_group_id = module.security_group.security_group_id
  public_subnet_id = module.vpc.public_subnet_id
}

module "s3" {
  source = "./s3"
}