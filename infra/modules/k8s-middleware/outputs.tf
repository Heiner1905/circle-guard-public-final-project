output "namespace" {
  description = "Namespace where the middleware releases are installed."
  value       = kubernetes_namespace.middleware.metadata[0].name
}

output "postgres_service_dns" {
  description = "In-cluster DNS for the PostgreSQL primary service."
  value       = "postgresql.${kubernetes_namespace.middleware.metadata[0].name}.svc.cluster.local"
}

output "neo4j_service_dns" {
  description = "In-cluster DNS for the Neo4j service."
  value       = "neo4j.${kubernetes_namespace.middleware.metadata[0].name}.svc.cluster.local"
}

output "kafka_bootstrap" {
  description = "In-cluster Kafka bootstrap server address."
  value       = "kafka.${kubernetes_namespace.middleware.metadata[0].name}.svc.cluster.local:9092"
}

output "redis_service_dns" {
  description = "In-cluster DNS for the Redis master service."
  value       = "redis-master.${kubernetes_namespace.middleware.metadata[0].name}.svc.cluster.local"
}

output "ldap_service_dns" {
  description = "In-cluster DNS for the OpenLDAP service."
  value       = "openldap.${kubernetes_namespace.middleware.metadata[0].name}.svc.cluster.local"
}
