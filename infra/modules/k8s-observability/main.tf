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
# Namespace for the observability stack.
# ----------------------------------------------------------------------------

resource "kubernetes_namespace" "observability" {
  metadata {
    name = var.namespace
    labels = {
      "app.kubernetes.io/part-of" = "circleguard"
      "circleguard.io/purpose"    = "observability"
    }
  }
}

# ----------------------------------------------------------------------------
# kube-prometheus-stack: Prometheus + Alertmanager + Grafana + Node Exporter
# + kube-state-metrics. ServiceMonitor selectors are *open* so any chart in
# the cluster can register without us editing helm values.
# ----------------------------------------------------------------------------

resource "helm_release" "kube_prometheus_stack" {
  name       = "kube-prometheus-stack"
  namespace  = kubernetes_namespace.observability.metadata[0].name
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "kube-prometheus-stack"
  version    = var.kube_prometheus_stack_version

  # Avoid stale CRDs from blocking re-installs in non-prod environments.
  atomic           = false
  cleanup_on_fail  = true
  timeout          = 900
  create_namespace = false

  values = [
    templatefile("${path.module}/values/kube-prometheus-stack.yaml.tftpl", {
      grafana_admin_user     = var.grafana_admin_user
      grafana_admin_password = var.grafana_admin_password
      grafana_persistence    = var.grafana_persistence_size
      prometheus_retention   = var.prometheus_retention
      prometheus_storage     = var.prometheus_storage_size
      prometheus_cpu_request = var.prometheus_cpu_request
      prometheus_mem_request = var.prometheus_mem_request
      prometheus_cpu_limit   = var.prometheus_cpu_limit
      prometheus_mem_limit   = var.prometheus_mem_limit
      grafana_cpu_request    = var.grafana_cpu_request
      grafana_mem_request    = var.grafana_mem_request
      grafana_cpu_limit      = var.grafana_cpu_limit
      grafana_mem_limit      = var.grafana_mem_limit
      alertmanager_storage   = var.alertmanager_storage_size
      alertmanager_retention = var.alertmanager_retention
    })
  ]

  depends_on = [kubernetes_namespace.observability]
}

# ----------------------------------------------------------------------------
# Grafana dashboards delivered as ConfigMaps. The Grafana sidecar (enabled in
# the values file) auto-discovers any ConfigMap labeled grafana_dashboard=1
# in this namespace and imports it.
# ----------------------------------------------------------------------------

resource "kubernetes_config_map" "grafana_dashboards" {
  for_each = var.dashboards_path == "" ? {} : {
    for f in fileset(var.dashboards_path, "*.json") :
    trimsuffix(f, ".json") => f
  }

  metadata {
    name      = "circleguard-dashboard-${each.key}"
    namespace = kubernetes_namespace.observability.metadata[0].name
    labels = {
      grafana_dashboard           = "1"
      "app.kubernetes.io/part-of" = "circleguard"
    }
  }

  data = {
    "${each.key}.json" = file("${var.dashboards_path}/${each.value}")
  }

  depends_on = [helm_release.kube_prometheus_stack]
}
