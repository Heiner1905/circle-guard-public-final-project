variable "resource_group_name" {
  description = "Resource Group that holds the remote tfstate Storage Account."
  type        = string
  default     = "rg-circleguard-tfstate"
}

variable "location" {
  description = "Azure region for the tfstate resources."
  type        = string
  default     = "eastus"
}

variable "storage_account_name" {
  description = "Globally unique Storage Account name. Lowercase alphanumeric, 3-24 chars."
  type        = string
  default     = "stcgtfstateh1905"

  validation {
    condition     = can(regex("^[a-z0-9]{3,24}$", var.storage_account_name))
    error_message = "Storage account name must be 3-24 chars, lowercase letters and digits only."
  }
}

variable "container_name" {
  description = "Blob container that stores the *.tfstate files for each environment."
  type        = string
  default     = "tfstate"
}
