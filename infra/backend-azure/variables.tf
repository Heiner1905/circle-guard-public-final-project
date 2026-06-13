variable "resource_group_name" {
  description = "Resource Group EXISTENTE que aloja el Storage Account del tfstate. No se crea (suscripción Azure con permisos acotados al RG 'CircleGuard')."
  type        = string
  default     = "CircleGuard"
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

variable "allowed_ip_rules" {
  description = "Public IPv4 addresses or CIDRs that may reach the tfstate Storage Account when network_rules.default_action = Deny. Default empty — AzureServices bypass cubre el caso común; añade aquí la IP saliente de tu runner GitHub Actions si necesitas acceso directo."
  type        = list(string)
  default     = []
}

variable "allowed_subnet_ids" {
  description = "Subnet IDs habilitados a alcanzar el Storage Account (Service Endpoint Microsoft.Storage). Útil cuando corres terraform desde una VM en una VNet de Azure."
  type        = list(string)
  default     = []
}
