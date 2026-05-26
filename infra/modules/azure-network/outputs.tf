output "vnet_id" {
  description = "ID of the VNet."
  value       = azurerm_virtual_network.this.id
}

output "vnet_name" {
  description = "Name of the VNet."
  value       = azurerm_virtual_network.this.name
}

output "aks_subnet_id" {
  description = "ID of the subnet AKS will use."
  value       = azurerm_subnet.aks.id
}

output "nsg_id" {
  description = "ID of the NSG attached to the AKS subnet."
  value       = azurerm_network_security_group.aks.id
}
