output "namespace" {
  description = "Namespace where the observability stack is deployed."
  value       = kubernetes_namespace.observability.metadata[0].name
}

output "grafana_service" {
  description = "In-cluster DNS of the Grafana service."
  value       = "kube-prometheus-stack-grafana.${kubernetes_namespace.observability.metadata[0].name}.svc.cluster.local"
}

output "prometheus_service" {
  description = "In-cluster DNS of the Prometheus service."
  value       = "kube-prometheus-stack-prometheus.${kubernetes_namespace.observability.metadata[0].name}.svc.cluster.local:9090"
}

output "alertmanager_service" {
  description = "In-cluster DNS of the Alertmanager service."
  value       = "kube-prometheus-stack-alertmanager.${kubernetes_namespace.observability.metadata[0].name}.svc.cluster.local:9093"
}

output "dashboards_loaded" {
  description = "Number of Grafana dashboards delivered as ConfigMaps."
  value       = length(kubernetes_config_map.grafana_dashboards)
}
