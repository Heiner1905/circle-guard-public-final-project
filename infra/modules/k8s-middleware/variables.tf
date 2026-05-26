variable "namespace" {
  description = "Kubernetes namespace where middleware Helm releases are installed."
  type        = string
  default     = "circleguard-middleware"
}

variable "persistence_size" {
  description = "Persistent volume size used by stateful charts (e.g. 8Gi, 16Gi, 32Gi)."
  type        = string
  default     = "8Gi"
}

# PostgreSQL --------------------------------------------------------------
variable "postgres_username" {
  description = "PostgreSQL application user."
  type        = string
  default     = "circleguard"
}

variable "postgres_password" {
  description = "PostgreSQL password for the application and postgres roles."
  type        = string
  sensitive   = true
}

variable "postgres_database" {
  description = "Default PostgreSQL database to create."
  type        = string
  default     = "circleguard"
}

# Neo4j -------------------------------------------------------------------
variable "neo4j_password" {
  description = "Neo4j password for the default 'neo4j' user."
  type        = string
  sensitive   = true
}

# Redis -------------------------------------------------------------------
variable "redis_password" {
  description = "Redis password."
  type        = string
  sensitive   = true
}

# OpenLDAP ----------------------------------------------------------------
variable "ldap_admin_user" {
  description = "OpenLDAP admin username."
  type        = string
  default     = "admin"
}

variable "ldap_admin_password" {
  description = "OpenLDAP admin password."
  type        = string
  sensitive   = true
}

variable "ldap_domain" {
  description = "Base LDAP domain (e.g. circleguard.local)."
  type        = string
  default     = "circleguard.local"
}
