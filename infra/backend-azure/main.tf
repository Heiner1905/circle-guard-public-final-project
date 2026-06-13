terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116"
    }
  }

  backend "azurerm" {
    resource_group_name  = "CircleGuard"
    storage_account_name = "tfstate1781322989"
    container_name       = "tfstate"
    key                  = "dev.terraform.tfstate"
    subscription_id      = "229c721d-e0d4-46dc-99fc-020b679d3ecc"
  }
}

provider "azurerm" {
  features {}

  skip_provider_registration = true
  storage_use_azuread        = false
}

locals {
  common_tags = {
    project   = "circleguard"
    managedBy = "terraform"
    purpose   = "remote-tfstate"
  }
}

data "azurerm_resource_group" "tfstate" {
  name = "CircleGuard"
}

resource "azurerm_storage_account" "tfstate" {
  name                     = "tfstate1781322989"
  resource_group_name      = data.azurerm_resource_group.tfstate.name
  location                 = data.azurerm_resource_group.tfstate.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"

  min_tls_version                 = "TLS1_2"
  allow_nested_items_to_be_public = false
  public_network_access_enabled   = true

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
  name                  = "tfstate"
  storage_account_name  = azurerm_storage_account.tfstate.name
  container_access_type = "private"
}