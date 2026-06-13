variable "namespace" {
  description = "Kubernetes namespace for the observability stack."
  type        = string
  default     = "circleguard-observability"
}

variable "kube_prometheus_stack_version" {
  description = "Helm chart version of prometheus-community/kube-prometheus-stack."
  type        = string
  default     = "65.5.0"
}

# ---------------------------------------------------------------------------
# Grafana
# ---------------------------------------------------------------------------
variable "grafana_admin_user" {
  description = "Grafana admin username."
  type        = string
  default     = "admin"
}

variable "grafana_admin_password" {
  description = "Grafana admin password. MUST be provided via secrets at deploy time."
  type        = string
  sensitive   = true
}

variable "grafana_persistence_size" {
  description = "PVC size for Grafana data."
  type        = string
  default     = "5Gi"
}

variable "grafana_cpu_request" {
  type    = string
  default = "50m"
}

variable "grafana_mem_request" {
  type    = string
  default = "128Mi"
}

variable "grafana_cpu_limit" {
  type    = string
  default = "300m"
}

variable "grafana_mem_limit" {
  type    = string
  default = "384Mi"
}

# ---------------------------------------------------------------------------
# Prometheus
# ---------------------------------------------------------------------------
variable "prometheus_retention" {
  description = "How long Prometheus keeps samples (e.g. \"7d\", \"15d\")."
  type        = string
  default     = "7d"
}

variable "prometheus_storage_size" {
  description = "PVC size for Prometheus TSDB."
  type        = string
  default     = "8Gi"
}

variable "prometheus_cpu_request" {
  type    = string
  default = "200m"
}

variable "prometheus_mem_request" {
  type    = string
  default = "512Mi"
}

variable "prometheus_cpu_limit" {
  type    = string
  default = "1"
}

variable "prometheus_mem_limit" {
  type    = string
  default = "1Gi"
}

# ---------------------------------------------------------------------------
# Alertmanager (rules + receivers come from US-12)
# ---------------------------------------------------------------------------
variable "alertmanager_storage_size" {
  type    = string
  default = "2Gi"
}

variable "alertmanager_retention" {
  type    = string
  default = "120h"
}

# ---------------------------------------------------------------------------
# Dashboards
# ---------------------------------------------------------------------------
variable "dashboards_path" {
  description = <<-EOT
    Absolute filesystem path containing Grafana dashboard JSONs. Every *.json
    inside becomes a ConfigMap with the grafana_dashboard=1 label, picked up
    by the Grafana sidecar. Set to "" to disable bundled dashboards.
  EOT
  type        = string
  default     = ""
}
