output "resource_group" {
  description = "Name of the Azure Resource Group hosting this environment."
  value       = azurerm_resource_group.this.name
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

output "aws_eks_cluster" {
  description = "Name of the EKS cluster (only when enable_aws=true)."
  value       = var.enable_aws ? module.aws_eks[0].cluster_name : null
}

output "aws_ecr_repository_urls" {
  description = "Map of microservice short name to ECR repo URL (only when enable_aws=true)."
  value       = var.enable_aws ? module.aws_ecr[0].repository_urls : null
}
