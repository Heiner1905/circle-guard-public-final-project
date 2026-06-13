variable "name_prefix" {
  description = "Short prefix used to name the VNet, subnet and NSG (e.g. circleguard-dev)."
  type        = string
}

variable "resource_group_name" {
  description = "Resource Group hosting the network resources."
  type        = string
}

variable "location" {
  description = "Azure region."
  type        = string
}

variable "vnet_cidr" {
  description = "Address space of the VNet."
  type        = string
  default     = "10.10.0.0/16"
}

variable "aks_subnet_cidr" {
  description = "Address prefix for the AKS node subnet."
  type        = string
  default     = "10.10.1.0/24"
}

variable "tags" {
  description = "Common tags applied to every resource."
  type        = map(string)
  default     = {}
}
