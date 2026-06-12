variable "extra_tags" {
  description = "Tags merged on top of project/managedBy/environment defaults."
  type        = map(string)
  default     = {}
}

# Azure -------------------------------------------------------------------
variable "azure_location" {
  description = "Azure region for this environment."
  type        = string
  default     = "eastus"
}

variable "vnet_cidr" {
  description = "Address space of the VNet."
  type        = string
  default     = "10.10.0.0/16"
}

variable "aks_subnet_cidr" {
  description = "Address prefix of the AKS subnet."
  type        = string
  default     = "10.10.1.0/24"
}

variable "kubernetes_version" {
  description = "AKS Kubernetes version."
  type        = string
  default     = "1.30.4"
}

variable "node_vm_size" {
  description = "VM SKU for AKS nodes."
  type        = string
  default     = "Standard_B2s"
}

variable "node_min_count" {
  description = "Minimum number of AKS nodes (autoscaler floor)."
  type        = number
  default     = 2
}

variable "node_max_count" {
  description = "Maximum number of AKS nodes (autoscaler ceiling)."
  type        = number
  default     = 3
}

variable "service_cidr" {
  description = "AKS service CIDR."
  type        = string
  default     = "172.16.0.0/16"
}

variable "dns_service_ip" {
  description = "AKS DNS service IP (must be inside service_cidr)."
  type        = string
  default     = "172.16.0.10"
}

variable "acr_name" {
  description = "ACR name (globally unique, lowercase alphanumeric)."
  type        = string
  default     = "acrcircleguarddev"
}

variable "acr_sku" {
  description = "ACR SKU."
  type        = string
  default     = "Basic"
}

# Middleware --------------------------------------------------------------
variable "middleware_namespace" {
  description = "Namespace for in-cluster middleware Helm releases."
  type        = string
  default     = "circleguard-middleware"
}

variable "middleware_persistence_size" {
  description = "PVC size for stateful middleware charts."
  type        = string
  default     = "8Gi"
}

variable "postgres_username" {
  description = "PostgreSQL application user."
  type        = string
  default     = "circleguard"
}

variable "postgres_password" {
  description = "PostgreSQL password."
  type        = string
  sensitive   = true
}

variable "postgres_database" {
  description = "PostgreSQL default database name."
  type        = string
  default     = "circleguard"
}

variable "neo4j_password" {
  description = "Neo4j password for the default 'neo4j' user."
  type        = string
  sensitive   = true
}

variable "redis_password" {
  description = "Redis password."
  type        = string
  sensitive   = true
}

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
  description = "Base LDAP domain."
  type        = string
  default     = "circleguard.local"
}

# AWS (optional) ----------------------------------------------------------
variable "enable_aws" {
  description = "Toggle to provision the AWS multi-cloud stack on top of Azure."
  type        = bool
  default     = false
}

variable "aws_region" {
  description = "AWS region (used only if enable_aws=true)."
  type        = string
  default     = "us-east-1"
}

variable "aws_vpc_cidr" {
  description = "CIDR block for the AWS VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "aws_kubernetes_version" {
  description = "EKS Kubernetes version."
  type        = string
  default     = "1.30"
}

variable "aws_node_instance_type" {
  description = "EC2 instance type for the EKS node group."
  type        = string
  default     = "t3.medium"
}

variable "aws_node_min_size" {
  description = "Minimum nodes in the EKS node group."
  type        = number
  default     = 1
}

variable "aws_node_max_size" {
  description = "Maximum nodes in the EKS node group."
  type        = number
  default     = 3
}

variable "aws_node_desired_size" {
  description = "Desired nodes in the EKS node group."
  type        = number
  default     = 2
}

# Observability (US-10) -------------------------------------------------------
variable "enable_observability" {
  description = "Toggle to install the kube-prometheus-stack + Grafana dashboards."
  type        = bool
  default     = true
}

variable "observability_namespace" {
  description = "Namespace for the observability stack."
  type        = string
  default     = "circleguard-observability"
}

variable "grafana_admin_user" {
  description = "Grafana admin username."
  type        = string
  default     = "admin"
}

variable "grafana_admin_password" {
  description = "Grafana admin password (set via CI secret)."
  type        = string
  sensitive   = true
}

variable "prometheus_retention" {
  description = "Prometheus sample retention window."
  type        = string
  default     = "7d"
}

variable "prometheus_storage_size" {
  description = "PVC size for Prometheus TSDB."
  type        = string
  default     = "8Gi"
}

variable "grafana_persistence_size" {
  description = "PVC size for Grafana data."
  type        = string
  default     = "5Gi"
}

variable "dashboards_path" {
  description = "Absolute path to a directory of Grafana dashboard JSONs to bundle."
  type        = string
  default     = ""
}

# Logging (US-11) ------------------------------------------------------------
variable "enable_logging" {
  description = "Toggle to install the Loki + Promtail logging stack."
  type        = bool
  default     = true
}

variable "loki_retention_hours" {
  description = "Hours of log retention before Loki compactor deletes chunks."
  type        = number
  default     = 72
}

variable "loki_storage_size" {
  description = "PVC size for Loki chunks/index."
  type        = string
  default     = "8Gi"
}

variable "enable_elk" {
  description = "Toggle the optional Elasticsearch + Kibana stack (ECK). Off in dev to save memory."
  type        = bool
  default     = false
}

# Tracing (US-12) ------------------------------------------------------------
variable "enable_tracing" {
  description = "Toggle to install the Jaeger tracing stack and the CircleGuard PrometheusRule alert bundle."
  type        = bool
  default     = true
}

variable "jaeger_memory_max_traces" {
  description = "Maximum traces kept in the Jaeger in-memory backend. Dev keeps a smaller buffer."
  type        = number
  default     = 25000
}

variable "enable_alerts" {
  description = "Whether to install the PrometheusRule CR with CircleGuard's alert family."
  type        = bool
  default     = true
}

# Security (US-15) -----------------------------------------------------------
variable "enable_security" {
  description = "Toggle to install the k8s-security module (PSA labels, ResourceQuota, LimitRange, cert-manager, ClusterIssuer)."
  type        = bool
  default     = true
}

variable "application_namespace" {
  description = "Application namespace (must match helm/circleguard/values.yaml global.namespace)."
  type        = string
  default     = "circleguard"
}

variable "psa_enforce_level" {
  description = "Pod Security Admission enforce level. Stays at baseline until Karold's deployment template adds seccompProfile=RuntimeDefault."
  type        = string
  default     = "baseline"
}

variable "psa_audit_level" {
  description = "Pod Security Admission audit level (logged to audit log without blocking)."
  type        = string
  default     = "restricted"
}

variable "psa_warn_level" {
  description = "Pod Security Admission warn level (kubectl warnings, no blocking)."
  type        = string
  default     = "restricted"
}

variable "enable_resource_quota" {
  description = "Install a ResourceQuota on the application namespace."
  type        = bool
  default     = true
}

variable "resource_quota_cpu_requests" {
  description = "Hard cap on total CPU requests in the application namespace."
  type        = string
  default     = "4"
}

variable "resource_quota_memory_requests" {
  description = "Hard cap on total memory requests in the application namespace."
  type        = string
  default     = "8Gi"
}

variable "resource_quota_cpu_limits" {
  description = "Hard cap on total CPU limits in the application namespace."
  type        = string
  default     = "8"
}

variable "resource_quota_memory_limits" {
  description = "Hard cap on total memory limits in the application namespace."
  type        = string
  default     = "16Gi"
}

variable "resource_quota_pods" {
  description = "Hard cap on pod count in the application namespace."
  type        = number
  default     = 30
}

variable "enable_cert_manager" {
  description = "Install the jetstack/cert-manager Helm release."
  type        = bool
  default     = true
}

variable "cert_manager_chart_version" {
  description = "Helm chart version for jetstack/cert-manager."
  type        = string
  default     = "v1.15.3"
}

variable "enable_cluster_issuer" {
  description = "Install the CircleGuard ClusterIssuer via the local circleguard-tls chart."
  type        = bool
  default     = true
}

variable "cluster_issuer_kind" {
  description = "Issuer kind for dev/stage/prod. selfsigned is fine for dev; flip to acme in stage/prod."
  type        = string
  default     = "selfsigned"
}

variable "cluster_issuer_acme_email" {
  description = "Contact email for ACME registration (required when cluster_issuer_kind=acme)."
  type        = string
  default     = ""
}

variable "cluster_issuer_acme_server" {
  description = "ACME directory URL. Defaults to Let's Encrypt staging."
  type        = string
  default     = "https://acme-staging-v02.api.letsencrypt.org/directory"
}

variable "cluster_issuer_acme_ingress_class" {
  description = "Ingress class name used by the HTTP-01 solver (must match helm/circleguard global.ingress.className)."
  type        = string
  default     = "nginx"
}
