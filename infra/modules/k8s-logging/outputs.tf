output "namespace" {
  description = "Namespace where the logging stack is deployed."
  value       = local.ns
}

output "loki_service" {
  description = "In-cluster DNS of the Loki service (Grafana points its datasource here)."
  value       = "http://loki-stack.${local.ns}.svc.cluster.local:3100"
}

output "promtail_status" {
  description = "Helm release name of the Promtail DaemonSet (deployed alongside Loki by loki-stack)."
  value       = helm_release.loki_stack.name
}

output "elasticsearch_service" {
  description = "In-cluster DNS of the Elasticsearch service when ELK is enabled."
  value       = var.enable_elk ? "https://circleguard-elasticsearch-es-http.${local.ns}.svc.cluster.local:9200" : null
}

output "kibana_service" {
  description = "In-cluster DNS of the Kibana service when ELK is enabled."
  value       = var.enable_elk ? "https://circleguard-kibana-kb-http.${local.ns}.svc.cluster.local:5601" : null
}
