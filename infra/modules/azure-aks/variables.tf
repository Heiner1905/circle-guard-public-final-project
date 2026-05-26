variable "name_prefix" {
  description = "Short prefix used to name the cluster (e.g. circleguard-dev)."
  type        = string
}

variable "resource_group_name" {
  description = "Resource Group hosting the AKS cluster."
  type        = string
}

variable "location" {
  description = "Azure region."
  type        = string
}

variable "aks_subnet_id" {
  description = "Subnet ID where AKS nodes are placed."
  type        = string
}

variable "kubernetes_version" {
  description = "AKS Kubernetes version."
  type        = string
  default     = "1.30.4"
}

variable "node_vm_size" {
  description = "VM SKU for the default node pool."
  type        = string
  default     = "Standard_B2s"
}

variable "node_min_count" {
  description = "Minimum number of nodes in the default pool (autoscaler floor)."
  type        = number
  default     = 2
}

variable "node_max_count" {
  description = "Maximum number of nodes in the default pool (autoscaler ceiling)."
  type        = number
  default     = 3
}

variable "service_cidr" {
  description = "Service CIDR used by the cluster (must not overlap the VNet)."
  type        = string
  default     = "172.16.0.0/16"
}

variable "dns_service_ip" {
  description = "DNS service IP inside service_cidr."
  type        = string
  default     = "172.16.0.10"
}

variable "tags" {
  description = "Common tags."
  type        = map(string)
  default     = {}
}
