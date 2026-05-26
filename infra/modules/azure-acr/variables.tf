variable "acr_name" {
  description = "Globally unique ACR name. Lowercase alphanumeric, 5-50 chars."
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9]{5,50}$", var.acr_name))
    error_message = "ACR name must be 5-50 chars, lowercase letters and digits only."
  }
}

variable "resource_group_name" {
  description = "Resource Group hosting the ACR."
  type        = string
}

variable "location" {
  description = "Azure region."
  type        = string
}

variable "sku" {
  description = "ACR SKU."
  type        = string
  default     = "Basic"

  validation {
    condition     = contains(["Basic", "Standard", "Premium"], var.sku)
    error_message = "ACR SKU must be Basic, Standard or Premium."
  }
}

variable "aks_kubelet_identity_object_id" {
  description = "Object ID of the AKS kubelet identity to grant AcrPull."
  type        = string
}

variable "tags" {
  description = "Common tags."
  type        = map(string)
  default     = {}
}
