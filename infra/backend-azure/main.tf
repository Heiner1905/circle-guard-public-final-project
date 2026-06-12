terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116"
    }
  }
}

provider "azurerm" {
  features {}
}

locals {
  common_tags = {
    project   = "circleguard"
    managedBy = "terraform"
    purpose   = "remote-tfstate"
  }
}

resource "azurerm_resource_group" "tfstate" {
  name     = var.resource_group_name
  location = var.location
  tags     = local.common_tags
}

resource "azurerm_storage_account" "tfstate" {
  name                     = var.storage_account_name
  resource_group_name      = azurerm_resource_group.tfstate.name
  location                 = azurerm_resource_group.tfstate.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"

  min_tls_version                 = "TLS1_2"
  allow_nested_items_to_be_public = false
  public_network_access_enabled   = true # GitHub Actions runners reach the SA over public network; SAS/RBAC + network_rules below gate access.

  # AZU-0012: deny-by-default. AzureServices bypass cubre backups, metrics y
  # cualquier servicio Azure (incluida la lectura del tfstate desde otros
  # módulos Terraform corriendo con identidad gestionada). Si tu runner de
  # GitHub Actions falla con 403, añade su IP saliente a `allowed_ip_rules`
  # en terraform.tfvars (variable definida en variables.tf).
  network_rules {
    default_action             = "Deny"
    bypass                     = ["AzureServices", "Logging", "Metrics"]
    ip_rules                   = var.allowed_ip_rules
    virtual_network_subnet_ids = var.allowed_subnet_ids
  }

  blob_properties {
    versioning_enabled = true

    delete_retention_policy {
      days = 7
    }

    container_delete_retention_policy {
      days = 7
    }
  }

  tags = local.common_tags
}

resource "azurerm_storage_container" "tfstate" {
  name                  = var.container_name
  storage_account_name  = azurerm_storage_account.tfstate.name
  container_access_type = "private"
}
