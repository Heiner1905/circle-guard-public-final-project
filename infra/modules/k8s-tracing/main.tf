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
# Namespace. Set create_namespace=false when reusing the observability
# namespace (US-10) so Grafana auto-discovers the Jaeger datasource ConfigMap.
# ----------------------------------------------------------------------------

resource "kubernetes_namespace" "tracing" {
  count = var.create_namespace ? 1 : 0

  metadata {
    name = var.namespace
    labels = {
      "app.kubernetes.io/part-of" = "circleguard"
      "circleguard.io/purpose"    = "tracing"
    }
  }
}

locals {
  ns = var.create_namespace ? kubernetes_namespace.tracing[0].metadata[0].name : var.namespace
}

# ----------------------------------------------------------------------------
# Jaeger all-in-one (agent + collector + query + UI in a single pod, memory
# backend). Suitable for academic / dev / stage workloads. For real production
# scale, swap to the distributed mode with a persistent backend (Elasticsearch
# already provisioned by k8s-logging when enable_elk=true).
# ----------------------------------------------------------------------------

resource "helm_release" "jaeger" {
  name       = "jaeger"
  namespace  = local.ns
  repository = "https://jaegertracing.github.io/helm-charts"
  chart      = "jaeger"
  version    = var.jaeger_chart_version

  atomic           = false
  cleanup_on_fail  = true
  timeout          = 600
  create_namespace = false

  values = [
    templatefile("${path.module}/values/jaeger.yaml.tftpl", {
      image_tag         = var.jaeger_image_tag
      memory_max_traces = var.jaeger_memory_max_traces
      cpu_request       = var.jaeger_cpu_request
      mem_request       = var.jaeger_mem_request
      cpu_limit         = var.jaeger_cpu_limit
      mem_limit         = var.jaeger_mem_limit
    })
  ]

  depends_on = [kubernetes_namespace.tracing]
}

# ----------------------------------------------------------------------------
# Grafana datasource for Jaeger, delivered as a ConfigMap labeled
# grafana_datasource=1 so the Grafana sidecar (enabled in US-10) auto-imports
# it within ~60 s. uid="jaeger" is fixed so the Loki derivedFields and any
# dashboard can reference the trace datasource deterministically.
# ----------------------------------------------------------------------------

resource "kubernetes_config_map" "jaeger_datasource" {
  metadata {
    name      = "circleguard-jaeger-datasource"
    namespace = local.ns
    labels = {
      grafana_datasource          = "1"
      "app.kubernetes.io/part-of" = "circleguard"
    }
  }

  data = {
    "jaeger-datasource.yaml" = yamlencode({
      apiVersion = 1
      datasources = [{
        name      = "Jaeger"
        uid       = "jaeger"
        type      = "jaeger"
        access    = "proxy"
        url       = "http://jaeger-query.${local.ns}.svc.cluster.local:16686"
        isDefault = false
        editable  = false
      }]
    })
  }

  depends_on = [helm_release.jaeger]
}

# ----------------------------------------------------------------------------
# Alertmanager rules: a tiny in-repo Helm chart (charts/circleguard-alerts)
# ships a single PrometheusRule CR with the CircleGuard alert family. The
# kube-prometheus-stack Prometheus picks it up via ruleSelectorNil=false (US-10).
# ----------------------------------------------------------------------------

resource "helm_release" "circleguard_alerts" {
  count = var.enable_alerts ? 1 : 0

  name      = "circleguard-alerts"
  namespace = local.ns
  chart     = "${path.module}/charts/circleguard-alerts"

  atomic          = false
  cleanup_on_fail = true
  timeout         = 300

  set {
    name  = "releaseLabel"
    value = var.alerts_release_label
  }

  set {
    name  = "serviceRegex"
    value = var.alert_service_regex
  }

  set {
    name  = "thresholds.errorRate"
    value = var.alert_error_rate_threshold
  }

  set {
    name  = "thresholds.latencyP99Seconds"
    value = var.alert_latency_p99_seconds
  }

  set {
    name  = "thresholds.heapUsageRatio"
    value = var.alert_heap_usage_ratio
  }

  depends_on = [kubernetes_namespace.tracing]
}
