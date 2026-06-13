variable "namespace" {
  description = "Kubernetes namespace for the tracing stack. Reuse the observability namespace so Grafana finds the Jaeger datasource without cross-namespace tokens."
  type        = string
  default     = "circleguard-observability"
}

variable "create_namespace" {
  description = "Whether this module should create the namespace. Set to false when the namespace is already managed by the observability module."
  type        = bool
  default     = false
}

# ---------------------------------------------------------------------------
# Jaeger (jaegertracing/jaeger Helm chart, all-in-one mode)
# ---------------------------------------------------------------------------
variable "jaeger_chart_version" {
  description = "Helm chart version of jaegertracing/jaeger."
  type        = string
  default     = "3.3.3"
}

variable "jaeger_image_tag" {
  description = "Jaeger image tag used by allInOne. Empty string lets the chart pick its default."
  type        = string
  default     = ""
}

variable "jaeger_memory_max_traces" {
  description = "Maximum number of traces kept in the in-memory backend before eviction."
  type        = number
  default     = 50000
}

variable "jaeger_cpu_request" {
  type    = string
  default = "100m"
}

variable "jaeger_mem_request" {
  type    = string
  default = "256Mi"
}

variable "jaeger_cpu_limit" {
  type    = string
  default = "500m"
}

variable "jaeger_mem_limit" {
  type    = string
  default = "512Mi"
}

# ---------------------------------------------------------------------------
# Alertmanager rules (PrometheusRule CRs, shipped via a tiny local Helm chart)
# ---------------------------------------------------------------------------
variable "enable_alerts" {
  description = "Whether to install the CircleGuard PrometheusRule CR with the default alert set."
  type        = bool
  default     = true
}

variable "alerts_release_label" {
  description = "Value of the 'release' label on PrometheusRule. The kube-prometheus-stack operator scrapes any namespace, so this is informational only (ruleSelectorNilUsesHelmValues=false in US-10), but staying consistent with the convention helps."
  type        = string
  default     = "kube-prometheus-stack"
}

variable "alert_service_regex" {
  description = "Regex used by the alert PromQL expressions to scope to CircleGuard services."
  type        = string
  default     = "circleguard-.*"
}

variable "alert_error_rate_threshold" {
  description = "5xx ratio over total requests, sustained for 5 min, that fires CircleGuardHighErrorRate."
  type        = number
  default     = 0.05
}

variable "alert_latency_p99_seconds" {
  description = "p99 latency (seconds) on http_server_requests, sustained for 5 min, that fires CircleGuardHighLatencyP99."
  type        = number
  default     = 1.0
}

variable "alert_heap_usage_ratio" {
  description = "JVM heap used/max ratio, sustained for 10 min, that fires CircleGuardHighHeapUsage."
  type        = number
  default     = 0.85
}
