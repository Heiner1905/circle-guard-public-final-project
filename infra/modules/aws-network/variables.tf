variable "name_prefix" {
  description = "Short prefix used to name VPC resources (e.g. circleguard-dev)."
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "cluster_name_tag" {
  description = "EKS cluster name used in the kubernetes.io/cluster/* tag for subnet discovery."
  type        = string
}

variable "tags" {
  description = "Common tags."
  type        = map(string)
  default     = {}
}
