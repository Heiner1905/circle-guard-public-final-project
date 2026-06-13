output "cluster_id" {
  description = "AKS cluster resource ID."
  value       = azurerm_kubernetes_cluster.this.id
}

output "cluster_name" {
  description = "AKS cluster name."
  value       = azurerm_kubernetes_cluster.this.name
}

output "kube_config_raw" {
  description = "Raw kubeconfig used to configure the kubernetes/helm providers."
  value       = azurerm_kubernetes_cluster.this.kube_config_raw
  sensitive   = true
}

output "kube_config" {
  description = "Structured kube_config block (host, certificates, token)."
  value       = azurerm_kubernetes_cluster.this.kube_config
  sensitive   = true
}

output "kubelet_identity_object_id" {
  description = "Object ID of the kubelet identity (used to grant AcrPull)."
  value       = azurerm_kubernetes_cluster.this.kubelet_identity[0].object_id
}

output "oidc_issuer_url" {
  description = "OIDC issuer URL of the cluster (Workload Identity federation)."
  value       = azurerm_kubernetes_cluster.this.oidc_issuer_url
}

output "node_resource_group" {
  description = "Auto-created MC_* resource group for node infrastructure."
  value       = azurerm_kubernetes_cluster.this.node_resource_group
}
