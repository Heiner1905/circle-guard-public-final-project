output "resource_group" {
  description = "Name of the resource group"
  value       = data.azurerm_resource_group.this.name
}

output "aks_cluster_name" {
  description = "Name of the AKS cluster (reusing dev cluster)"
  value       = data.azurerm_kubernetes_cluster.this.name
}

output "acr_login_server" {
  description = "ACR login server (reusing dev ACR)"
  value       = data.azurerm_container_registry.this.login_server
}

output "middleware_namespace" {
  description = "Kubernetes namespace where middleware is deployed"
  value       = var.middleware_namespace
}

output "observability_namespace" {
  description = "Kubernetes namespace for observability stack"
  value       = var.observability_namespace
}

output "prometheus_service" {
  description = "Prometheus service DNS name"
  value       = "kube-prometheus-stack-prometheus.${var.observability_namespace}.svc.cluster.local:9090"
}

output "grafana_service" {
  description = "Grafana service DNS name"
  value       = "kube-prometheus-stack-grafana.${var.observability_namespace}.svc.cluster.local:80"
}

output "loki_service" {
  description = "Loki service DNS name"
  value       = "loki-stack.${var.observability_namespace}.svc.cluster.local:3100"
}

output "jaeger_query_url" {
  description = "Jaeger query service URL"
  value       = "http://jaeger-query.${var.observability_namespace}.svc.cluster.local:16686"
}

output "jaeger_otlp_endpoint" {
  description = "Jaeger OTLP endpoint for traces"
  value       = "http://jaeger-collector.${var.observability_namespace}.svc.cluster.local:4318/v1/traces"
}

output "kafka_bootstrap" {
  description = "Kafka bootstrap server address"
  value       = "kafka.${var.middleware_namespace}.svc.cluster.local:9092"
}