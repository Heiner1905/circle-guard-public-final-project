output "resource_group_name" {
  description = "Resource Group hosting the tfstate Storage Account."
  value       = data.azurerm_resource_group.tfstate.name
}

output "storage_account_name" {
  description = "Storage Account name to reference in environment backend blocks."
  value       = azurerm_storage_account.tfstate.name
}

output "container_name" {
  description = "Container name where the *.tfstate files live."
  value       = azurerm_storage_container.tfstate.name
}

output "backend_config_snippet" {
  description = "Copy/paste-ready backend config snippet for environments."
  value       = <<-EOT
    terraform {
      backend "azurerm" {
        resource_group_name  = "${data.azurerm_resource_group.tfstate.name}"
        storage_account_name = "${azurerm_storage_account.tfstate.name}"
        container_name       = "${azurerm_storage_container.tfstate.name}"
        key                  = "<env>.terraform.tfstate"
      }
    }
  EOT
}
