variable "name_prefix" {
  description = "Suffix for IAM role names (e.g. circleguard-dev)."
  type        = string
}

variable "cluster_name" {
  description = "EKS cluster name."
  type        = string
}

variable "kubernetes_version" {
  description = "EKS Kubernetes version."
  type        = string
  default     = "1.30"
}

variable "subnet_ids" {
  description = "Subnets where the control plane ENIs and node group are placed."
  type        = list(string)
}

variable "node_instance_type" {
  description = "EC2 instance type for the default node group."
  type        = string
  default     = "t3.medium"
}

variable "node_min_size" {
  description = "Minimum number of nodes."
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Maximum number of nodes."
  type        = number
  default     = 3
}

variable "node_desired_size" {
  description = "Desired number of nodes."
  type        = number
  default     = 2
}

variable "tags" {
  description = "Common tags."
  type        = map(string)
  default     = {}
}
