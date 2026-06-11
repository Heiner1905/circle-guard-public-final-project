# ---------------------------------------------------------------------------
# Target namespace (CircleGuard application namespace, owned by Karold's chart)
# ---------------------------------------------------------------------------
variable "circleguard_namespace" {
  description = "Application namespace where the microservices live (owned by Karold's helm chart). PSA labels, ResourceQuota and LimitRange are applied to this namespace."
  type        = string
  default     = "circleguard"
}

# ---------------------------------------------------------------------------
# Pod Security Admission
# ---------------------------------------------------------------------------
variable "psa_enforce_level" {
  description = "Pod Security Admission enforce level. Karold's deployment template satisfies 'baseline' today but not 'restricted' (missing seccompProfile=RuntimeDefault). Keep 'baseline' until the chart is upgraded."
  type        = string
  default     = "baseline"

  validation {
    condition     = contains(["privileged", "baseline", "restricted"], var.psa_enforce_level)
    error_message = "psa_enforce_level must be one of privileged, baseline, restricted."
  }
}

variable "psa_audit_level" {
  description = "Pod Security Admission audit level. Logs violations of this profile to the API server audit log without blocking them."
  type        = string
  default     = "restricted"

  validation {
    condition     = contains(["privileged", "baseline", "restricted"], var.psa_audit_level)
    error_message = "psa_audit_level must be one of privileged, baseline, restricted."
  }
}

variable "psa_warn_level" {
  description = "Pod Security Admission warn level. Returns warnings to kubectl when manifests violate the profile."
  type        = string
  default     = "restricted"

  validation {
    condition     = contains(["privileged", "baseline", "restricted"], var.psa_warn_level)
    error_message = "psa_warn_level must be one of privileged, baseline, restricted."
  }
}

# ---------------------------------------------------------------------------
# ResourceQuota
# ---------------------------------------------------------------------------
variable "enable_resource_quota" {
  description = "Install a ResourceQuota on the CircleGuard namespace."
  type        = bool
  default     = true
}

variable "resource_quota_cpu_requests" {
  description = "Hard cap on the sum of all container CPU requests in the namespace."
  type        = string
  default     = "4"
}

variable "resource_quota_memory_requests" {
  description = "Hard cap on the sum of all container memory requests in the namespace."
  type        = string
  default     = "8Gi"
}

variable "resource_quota_cpu_limits" {
  description = "Hard cap on the sum of all container CPU limits in the namespace."
  type        = string
  default     = "8"
}

variable "resource_quota_memory_limits" {
  description = "Hard cap on the sum of all container memory limits in the namespace."
  type        = string
  default     = "16Gi"
}

variable "resource_quota_pods" {
  description = "Maximum number of pods allowed in the namespace."
  type        = number
  default     = 30
}

# ---------------------------------------------------------------------------
# LimitRange
# ---------------------------------------------------------------------------
variable "enable_limit_range" {
  description = "Install a LimitRange on the CircleGuard namespace."
  type        = bool
  default     = true
}

variable "limit_range_default_cpu_request" {
  description = "Default container CPU request when a pod does not declare one."
  type        = string
  default     = "100m"
}

variable "limit_range_default_memory_request" {
  description = "Default container memory request when a pod does not declare one."
  type        = string
  default     = "256Mi"
}

variable "limit_range_default_cpu_limit" {
  description = "Default container CPU limit when a pod does not declare one."
  type        = string
  default     = "500m"
}

variable "limit_range_default_memory_limit" {
  description = "Default container memory limit when a pod does not declare one."
  type        = string
  default     = "512Mi"
}

# ---------------------------------------------------------------------------
# cert-manager (TLS issuance)
# ---------------------------------------------------------------------------
variable "enable_cert_manager" {
  description = "Install the jetstack/cert-manager Helm release together with CRDs and Prometheus ServiceMonitor."
  type        = bool
  default     = true
}

variable "cert_manager_namespace" {
  description = "Namespace for cert-manager. The CRDs install cluster-wide regardless of this value."
  type        = string
  default     = "cert-manager"
}

variable "cert_manager_chart_version" {
  description = "Helm chart version for jetstack/cert-manager."
  type        = string
  default     = "v1.15.3"
}

# ---------------------------------------------------------------------------
# ClusterIssuer (shipped via the local circleguard-tls chart)
# ---------------------------------------------------------------------------
variable "enable_cluster_issuer" {
  description = "Install the CircleGuard ClusterIssuer. Requires enable_cert_manager=true."
  type        = bool
  default     = true
}

variable "cluster_issuer_name" {
  description = "Name of the ClusterIssuer that Ingress annotations will reference."
  type        = string
  default     = "circleguard-issuer"
}

variable "cluster_issuer_kind" {
  description = "Issuer kind. 'selfsigned' is fine for dev / academic clusters; 'acme' uses Let's Encrypt for stage / prod."
  type        = string
  default     = "selfsigned"

  validation {
    condition     = contains(["selfsigned", "acme"], var.cluster_issuer_kind)
    error_message = "cluster_issuer_kind must be either selfsigned or acme."
  }
}

variable "cluster_issuer_acme_email" {
  description = "Contact email for ACME registration. Required when cluster_issuer_kind=acme."
  type        = string
  default     = ""
}

variable "cluster_issuer_acme_server" {
  description = "ACME server URL. Defaults to Let's Encrypt staging so a misconfigured stage env does not exhaust the prod rate limits."
  type        = string
  default     = "https://acme-staging-v02.api.letsencrypt.org/directory"
}

variable "cluster_issuer_acme_ingress_class" {
  description = "Ingress class name used by the HTTP-01 challenge solver. Must match Karold's global.ingress.className (default nginx)."
  type        = string
  default     = "nginx"
}
