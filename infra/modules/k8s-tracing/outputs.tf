output "namespace" {
  description = "Namespace where the tracing stack is deployed."
  value       = local.ns
}

output "jaeger_query_url" {
  description = "In-cluster DNS of the Jaeger query (UI) service."
  value       = "http://jaeger-query.${local.ns}.svc.cluster.local:16686"
}

output "jaeger_otlp_grpc_endpoint" {
  description = "OTLP gRPC endpoint that Spring Boot tracers must point to in-cluster."
  value       = "http://jaeger-collector.${local.ns}.svc.cluster.local:4317"
}

output "jaeger_otlp_http_endpoint" {
  description = "OTLP HTTP endpoint with the /v1/traces suffix (used by Micrometer Tracing's OTLP exporter)."
  value       = "http://jaeger-collector.${local.ns}.svc.cluster.local:4318/v1/traces"
}

output "alerts_release" {
  description = "Helm release name of the CircleGuard PrometheusRule bundle (null when enable_alerts=false)."
  value       = var.enable_alerts ? "circleguard-alerts" : null
}
