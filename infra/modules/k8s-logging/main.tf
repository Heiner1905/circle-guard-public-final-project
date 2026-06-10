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
# Namespace. Set `create_namespace = false` when reusing the observability
# namespace from US-10 so Grafana can scrape Loki via a sidecar-discovered
# datasource without cross-namespace ServiceAccount tokens.
# ----------------------------------------------------------------------------

resource "kubernetes_namespace" "logging" {
  count = var.create_namespace ? 1 : 0

  metadata {
    name = var.namespace
    labels = {
      "app.kubernetes.io/part-of" = "circleguard"
      "circleguard.io/purpose"    = "logging"
    }
  }
}

locals {
  ns = var.create_namespace ? kubernetes_namespace.logging[0].metadata[0].name : var.namespace
}

# ----------------------------------------------------------------------------
# Loki single-binary + Promtail DaemonSet via grafana/loki-stack.
# Grafana subchart is disabled (the kube-prometheus-stack from US-10 owns it).
# ----------------------------------------------------------------------------

resource "helm_release" "loki_stack" {
  name       = "loki-stack"
  namespace  = local.ns
  repository = "https://grafana.github.io/helm-charts"
  chart      = "loki-stack"
  version    = var.loki_stack_version

  atomic           = false
  cleanup_on_fail  = true
  timeout          = 600
  create_namespace = false

  values = [
    templatefile("${path.module}/values/loki-stack.yaml.tftpl", {
      loki_retention_hours = var.loki_retention_hours
      loki_storage_size    = var.loki_storage_size
      loki_cpu_request     = var.loki_cpu_request
      loki_mem_request     = var.loki_mem_request
      loki_cpu_limit       = var.loki_cpu_limit
      loki_mem_limit       = var.loki_mem_limit
      promtail_cpu_request = var.promtail_cpu_request
      promtail_mem_request = var.promtail_mem_request
      promtail_cpu_limit   = var.promtail_cpu_limit
      promtail_mem_limit   = var.promtail_mem_limit
    })
  ]

  depends_on = [kubernetes_namespace.logging]
}

# ----------------------------------------------------------------------------
# ECK (Elastic Cloud on Kubernetes) — opt-in via enable_elk. Two-step install:
# (1) eck-operator chart provisions CRDs + operator deployment; (2) eck-stack
# chart provisions the Elasticsearch + Kibana custom resources, with the
# nested operator subchart disabled to avoid double installs.
# ----------------------------------------------------------------------------

resource "helm_release" "eck_operator" {
  count = var.enable_elk ? 1 : 0

  name       = "eck-operator"
  namespace  = local.ns
  repository = "https://helm.elastic.co"
  chart      = "eck-operator"
  version    = var.eck_operator_version

  atomic          = false
  cleanup_on_fail = true
  timeout         = 600

  set {
    name  = "installCRDs"
    value = "true"
  }

  depends_on = [kubernetes_namespace.logging]
}

# ----------------------------------------------------------------------------
# Grafana datasource for Loki, delivered as a ConfigMap with the
# grafana_datasource=1 label. The kube-prometheus-stack Grafana sidecar
# auto-imports it on the next reconcile loop (within ~60 s).
# Uses a fixed uid="loki" so dashboards can reference it deterministically.
# ----------------------------------------------------------------------------

resource "kubernetes_config_map" "loki_datasource" {
  metadata {
    name      = "circleguard-loki-datasource"
    namespace = local.ns
    labels = {
      grafana_datasource          = "1"
      "app.kubernetes.io/part-of" = "circleguard"
    }
  }

  data = {
    "loki-datasource.yaml" = yamlencode({
      apiVersion = 1
      datasources = [{
        name      = "Loki"
        uid       = "loki"
        type      = "loki"
        access    = "proxy"
        url       = "http://loki-stack:3100"
        isDefault = false
        editable  = false
        jsonData = {
          maxLines = 1000
        }
      }]
    })
  }

  depends_on = [helm_release.loki_stack]
}

resource "helm_release" "eck_stack" {
  count = var.enable_elk ? 1 : 0

  name       = "eck-stack"
  namespace  = local.ns
  repository = "https://helm.elastic.co"
  chart      = "eck-stack"
  version    = var.eck_stack_version

  atomic          = false
  cleanup_on_fail = true
  timeout         = 900

  values = [
    templatefile("${path.module}/values/eck-stack.yaml.tftpl", {
      es_version         = var.elastic_version
      es_node_count      = var.elasticsearch_node_count
      es_storage_size    = var.elasticsearch_storage_size
      es_cpu_request     = var.elasticsearch_cpu_request
      es_mem_request     = var.elasticsearch_mem_request
      es_cpu_limit       = var.elasticsearch_cpu_limit
      es_mem_limit       = var.elasticsearch_mem_limit
      kibana_cpu_request = var.kibana_cpu_request
      kibana_mem_request = var.kibana_mem_request
      kibana_cpu_limit   = var.kibana_cpu_limit
      kibana_mem_limit   = var.kibana_mem_limit
    })
  ]

  depends_on = [helm_release.eck_operator]
}
