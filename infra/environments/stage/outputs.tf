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