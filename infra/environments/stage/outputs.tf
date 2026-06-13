output "resource_group" {
  description = "Name of the Azure Resource Group hosting this environment."
  value       = data.azurerm_resource_group.this.name
}

output "aks_cluster_name" {
  description = "Name of the AKS cluster."
  value       = module.aks.cluster_name
}

output "acr_login_server" {
  description = "ACR login server (push/pull endpoint)."
  value       = module.acr.login_server
}

output "middleware_namespace" {
  description = "Kubernetes namespace where middleware lives."
  value       = module.middleware.namespace
}

output "kafka_bootstrap" {
  description = "In-cluster Kafka bootstrap server."
  value       = module.middleware.kafka_bootstrap
}

output "observability_namespace" {
  description = "Namespace running Prometheus / Grafana (null when disabled)."
  value       = var.enable_observability ? module.observability[0].namespace : null
}

output "grafana_service" {
  description = "In-cluster DNS of Grafana (null when disabled). Port-forward to view dashboards."
  value       = var.enable_observability ? module.observability[0].grafana_service : null
}

output "prometheus_service" {
  description = "In-cluster DNS of Prometheus (null when disabled)."
  value       = var.enable_observability ? module.observability[0].prometheus_service : null
}

output "loki_service" {
  description = "In-cluster DNS of Loki (null when disabled). Add as Grafana datasource."
  value       = var.enable_logging ? module.logging[0].loki_service : null
}

output "kibana_service" {
  description = "In-cluster DNS of Kibana (null when ELK is disabled)."
  value       = var.enable_logging && var.enable_elk ? module.logging[0].kibana_service : null
}

output "jaeger_query_url" {
  description = "In-cluster DNS of the Jaeger query UI (null when tracing is disabled)."
  value       = var.enable_tracing ? module.tracing[0].jaeger_query_url : null
}

output "jaeger_otlp_endpoint" {
  description = "OTLP HTTP endpoint for Spring Boot tracers (null when tracing is disabled)."
  value       = var.enable_tracing ? module.tracing[0].jaeger_otlp_http_endpoint : null
}

output "cluster_issuer_name" {
  description = "Name of the ClusterIssuer to reference in Ingress annotations (null when security or issuer is disabled)."
  value       = var.enable_security ? module.security[0].cluster_issuer_name : null
}

output "psa_levels" {
  description = "Pod Security Admission levels effectively applied to the application namespace (null when security is disabled)."
  value       = var.enable_security ? module.security[0].psa_levels : null
}