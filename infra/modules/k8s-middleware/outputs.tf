output "namespace" {
  description = "Kubernetes namespace where middleware is deployed"
  value       = var.namespace
}

output "postgres_service_dns" {
  description = "DNS name of the PostgreSQL service"
  value       = "postgresql.${var.namespace}.svc.cluster.local"
}

output "neo4j_service_dns" {
  description = "DNS name of the Neo4j service"
  value       = "neo4j.${var.namespace}.svc.cluster.local"
}

output "kafka_bootstrap" {
  description = "Kafka bootstrap server address"
  value       = "kafka.${var.namespace}.svc.cluster.local:9092"
}

output "redis_service_dns" {
  description = "DNS name of the Redis master service"
  value       = "redis-master.${var.namespace}.svc.cluster.local"
}

output "ldap_service_dns" {
  description = "DNS name of the OpenLDAP service"
  value       = "openldap.${var.namespace}.svc.cluster.local"
}