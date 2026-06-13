variable "repo_prefix" {
  description = "Prefix for every ECR repo (final name is '<prefix>/<service>')."
  type        = string
  default     = "circleguard"
}

variable "service_names" {
  description = "List of microservice short names that get their own ECR repo."
  type        = list(string)
  default = [
    "auth-service",
    "identity-service",
    "promotion-service",
    "notification-service",
    "form-service",
    "gateway-service",
    "dashboard-service",
    "file-service",
  ]
}

variable "tags" {
  description = "Common tags."
  type        = map(string)
  default     = {}
}
