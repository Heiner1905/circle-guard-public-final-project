terraform {
  required_providers {
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.14"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.31"
    }
  }
}

# ----------------------------------------------------------------------------
# Pod Security Admission labels on the CircleGuard application namespace.
#
# Karold's helm chart owns the namespace object, so this module patches it
# in-place via kubernetes_labels (server-side apply, no ownership conflict).
#
# Default enforce level is "baseline" because Karold's deployment template
# already satisfies it (runAsNonRoot, drop ALL caps, allowPrivilegeEscalation
# false) but does NOT yet set seccompProfile=RuntimeDefault which is required
# by "restricted". We still audit + warn at "restricted" so the gap is visible.
# ----------------------------------------------------------------------------

resource "kubernetes_labels" "circleguard_psa" {
  api_version = "v1"
  kind        = "Namespace"

  metadata {
    name = var.circleguard_namespace
  }

  labels = {
    "pod-security.kubernetes.io/enforce" = var.psa_enforce_level
    "pod-security.kubernetes.io/audit"   = var.psa_audit_level
    "pod-security.kubernetes.io/warn"    = var.psa_warn_level
  }

  field_manager = "circleguard-security"
  force         = true
}

# ----------------------------------------------------------------------------
# ResourceQuota - cap on aggregate CPU / memory / pod count for the namespace.
# Acts as a tenant-level guardrail so a runaway service cannot starve the rest
# of the cluster. Tuned per env via variables.
# ----------------------------------------------------------------------------

resource "kubernetes_resource_quota_v1" "circleguard" {
  count = var.enable_resource_quota ? 1 : 0

  metadata {
    name      = "circleguard-quota"
    namespace = var.circleguard_namespace
    labels = {
      "app.kubernetes.io/part-of" = "circleguard"
      "circleguard.io/purpose"    = "resource-quota"
    }
  }

  spec {
    hard = {
      "requests.cpu"    = var.resource_quota_cpu_requests
      "requests.memory" = var.resource_quota_memory_requests
      "limits.cpu"      = var.resource_quota_cpu_limits
      "limits.memory"   = var.resource_quota_memory_limits
      "pods"            = var.resource_quota_pods
    }
  }

  depends_on = [kubernetes_labels.circleguard_psa]
}

# ----------------------------------------------------------------------------
# LimitRange - default container requests/limits so anyone deploying a Pod
# without explicit resources still gets sensible defaults. Karold's template
# already sets resources, but this protects manually-deployed sidecars/Jobs.
# ----------------------------------------------------------------------------

resource "kubernetes_limit_range_v1" "circleguard" {
  count = var.enable_limit_range ? 1 : 0

  metadata {
    name      = "circleguard-limits"
    namespace = var.circleguard_namespace
    labels = {
      "app.kubernetes.io/part-of" = "circleguard"
      "circleguard.io/purpose"    = "limit-range"
    }
  }

  spec {
    limit {
      type = "Container"
      default = {
        cpu    = var.limit_range_default_cpu_limit
        memory = var.limit_range_default_memory_limit
      }
      default_request = {
        cpu    = var.limit_range_default_cpu_request
        memory = var.limit_range_default_memory_request
      }
    }
  }

  depends_on = [kubernetes_labels.circleguard_psa]
}

# ----------------------------------------------------------------------------
# cert-manager - cluster-wide TLS issuance via ACME (Let's Encrypt) or self-
# signed. Installed in its own namespace; CRDs bundled so no manual kubectl.
# Prometheus scraping is enabled so US-10 picks up issuance/renewal metrics.
# ----------------------------------------------------------------------------

resource "kubernetes_namespace" "cert_manager" {
  count = var.enable_cert_manager ? 1 : 0

  metadata {
    name = var.cert_manager_namespace
    labels = {
      "app.kubernetes.io/part-of"          = "circleguard"
      "circleguard.io/purpose"             = "tls"
      "pod-security.kubernetes.io/enforce" = "baseline"
    }
  }
}

resource "helm_release" "cert_manager" {
  count = var.enable_cert_manager ? 1 : 0

  name       = "cert-manager"
  namespace  = var.cert_manager_namespace
  repository = "https://charts.jetstack.io"
  chart      = "cert-manager"
  version    = var.cert_manager_chart_version

  atomic          = false
  cleanup_on_fail = true
  timeout         = 600

  set {
    name  = "installCRDs"
    value = "true"
  }

  set {
    name  = "prometheus.enabled"
    value = "true"
  }

  set {
    name  = "prometheus.servicemonitor.enabled"
    value = "true"
  }

  depends_on = [kubernetes_namespace.cert_manager]
}

# ----------------------------------------------------------------------------
# ClusterIssuer - delivered through a tiny in-repo Helm chart so the manifest
# can be rendered offline (kubernetes_manifest needs a live cluster). The chart
# emits either a SelfSigned issuer (dev) or an ACME issuer (stage / prod)
# depending on var.cluster_issuer_kind.
# ----------------------------------------------------------------------------

resource "helm_release" "circleguard_tls" {
  count = var.enable_cert_manager && var.enable_cluster_issuer ? 1 : 0

  name      = "circleguard-tls"
  namespace = var.cert_manager_namespace
  chart     = "${path.module}/charts/circleguard-tls"

  atomic          = false
  cleanup_on_fail = true
  timeout         = 300

  set {
    name  = "issuer.name"
    value = var.cluster_issuer_name
  }

  set {
    name  = "issuer.kind"
    value = var.cluster_issuer_kind
  }

  set {
    name  = "issuer.acme.email"
    value = var.cluster_issuer_acme_email
  }

  set {
    name  = "issuer.acme.server"
    value = var.cluster_issuer_acme_server
  }

  set {
    name  = "issuer.acme.ingressClass"
    value = var.cluster_issuer_acme_ingress_class
  }

  depends_on = [helm_release.cert_manager]
}
