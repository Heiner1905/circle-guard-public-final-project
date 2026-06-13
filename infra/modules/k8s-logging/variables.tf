variable "namespace" {
  description = "Kubernetes namespace for the logging stack. Reuse the observability namespace to share Grafana."
  type        = string
  default     = "circleguard-observability"
}

variable "tracing_datasource_uid" {
  description = "UID of a Grafana tracing datasource (e.g. \"jaeger\") to wire as a derivedField on the Loki datasource so traceId values become click-through links to traces. Empty string disables the derived field."
  type        = string
  default     = ""
}

variable "create_namespace" {
  description = "Whether this module should create the namespace. Set to false when the namespace is already managed by the observability module."
  type        = bool
  default     = false
}

# ---------------------------------------------------------------------------
# Loki + Promtail (grafana/loki-stack)
# ---------------------------------------------------------------------------
variable "loki_stack_version" {
  description = "Helm chart version of grafana/loki-stack."
  type        = string
  default     = "2.10.2"
}

variable "loki_retention_hours" {
  description = "Hours of log retention before the Loki compactor deletes chunks."
  type        = number
  default     = 168
}

variable "loki_storage_size" {
  description = "PVC size for Loki chunks/index."
  type        = string
  default     = "10Gi"
}

variable "loki_cpu_request" {
  type    = string
  default = "100m"
}

variable "loki_mem_request" {
  type    = string
  default = "256Mi"
}

variable "loki_cpu_limit" {
  type    = string
  default = "500m"
}

variable "loki_mem_limit" {
  type    = string
  default = "512Mi"
}

variable "promtail_cpu_request" {
  type    = string
  default = "50m"
}

variable "promtail_mem_request" {
  type    = string
  default = "64Mi"
}

variable "promtail_cpu_limit" {
  type    = string
  default = "200m"
}

variable "promtail_mem_limit" {
  type    = string
  default = "128Mi"
}

# ---------------------------------------------------------------------------
# ECK (Elasticsearch + Kibana) — opt-in
# ---------------------------------------------------------------------------
variable "enable_elk" {
  description = "Enable the Elasticsearch + Kibana stack via ECK. Disabled by default to keep node footprint small."
  type        = bool
  default     = false
}

variable "eck_operator_version" {
  description = "Helm chart version of elastic/eck-operator."
  type        = string
  default     = "2.13.0"
}

variable "eck_stack_version" {
  description = "Helm chart version of elastic/eck-stack."
  type        = string
  default     = "0.10.0"
}

variable "elastic_version" {
  description = "Version of Elasticsearch + Kibana shipped by ECK."
  type        = string
  default     = "8.13.4"
}

variable "elasticsearch_node_count" {
  description = "Number of Elasticsearch nodes. Use 1 for dev (single shard, no quorum)."
  type        = number
  default     = 1
}

variable "elasticsearch_storage_size" {
  type    = string
  default = "10Gi"
}

variable "elasticsearch_cpu_request" {
  type    = string
  default = "500m"
}

variable "elasticsearch_mem_request" {
  type    = string
  default = "2Gi"
}

variable "elasticsearch_cpu_limit" {
  type    = string
  default = "1"
}

variable "elasticsearch_mem_limit" {
  type    = string
  default = "2Gi"
}

variable "kibana_cpu_request" {
  type    = string
  default = "100m"
}

variable "kibana_mem_request" {
  type    = string
  default = "512Mi"
}

variable "kibana_cpu_limit" {
  type    = string
  default = "500m"
}

variable "kibana_mem_limit" {
  type    = string
  default = "1Gi"
}

# ---------------------------------------------------------------------------
# Logstash (ELK pipeline parser: Filebeat → Logstash → Elasticsearch)
# ---------------------------------------------------------------------------
variable "logstash_cpu_request" {
  type    = string
  default = "200m"
}

variable "logstash_mem_request" {
  type    = string
  default = "1Gi"
}

variable "logstash_cpu_limit" {
  type    = string
  default = "1"
}

variable "logstash_mem_limit" {
  type    = string
  default = "1536Mi"
}

# ---------------------------------------------------------------------------
# Filebeat (DaemonSet — one pod per node tailing /var/log/containers)
# ---------------------------------------------------------------------------
variable "filebeat_cpu_request" {
  type    = string
  default = "100m"
}

variable "filebeat_mem_request" {
  type    = string
  default = "128Mi"
}

variable "filebeat_cpu_limit" {
  type    = string
  default = "500m"
}

variable "filebeat_mem_limit" {
  type    = string
  default = "256Mi"
}
