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

  # La suscripción de despliegue (729bf021-...) no concede permisos a nivel
  # de suscripción — solo Contributor sobre el RG "CircleGuard". Sin esto,
  # el provider intenta registrar Microsoft.Storage / Microsoft.Network /
  # etc. y rompe con "Terraform does not have the necessary permissions to
  # register Resource Providers". Los providers ya están registrados a
  # nivel de suscripción por el owner; sólo nos toca consumirlos.
  # NOTA: directiva válida para azurerm v3.x. Si en algún momento subimos
  # a v4.x hay que migrar a `resource_provider_registrations = "none"`.
  skip_provider_registration = true

  # El usuario que corre `terraform apply` tiene rol Contributor sobre el RG
  # (management plane) pero NO tiene rol de datos sobre blobs (data plane:
  # "Storage Blob Data Contributor"). Sin esto, al crear el
  # azurerm_storage_container el provider intenta autenticarse contra el
  # data plane vía Azure AD y rompe con 403 AuthorizationFailure. Con
  # storage_use_azuread=false el provider usa la access key del Storage
  # Account (la lista vía management plane, donde sí tenemos permiso),
  # y la operación de blobs/containers funciona.
  storage_use_azuread = false
}

locals {
  common_tags = {
    project   = "circleguard"
    managedBy = "terraform"
    purpose   = "remote-tfstate"
  }
}

# Suscripción con permisos limitados: el rol Contributor está acotado al RG
# existente "CircleGuard" — no podemos crear Resource Groups. Por eso usamos
# un data source en vez de `resource "azurerm_resource_group"`. La variable
# `location` queda como informativa (no se aplica al RG, viene del existente).
data "azurerm_resource_group" "tfstate" {
  name = var.resource_group_name
}

resource "azurerm_storage_account" "tfstate" {
  name                     = var.storage_account_name
  resource_group_name      = data.azurerm_resource_group.tfstate.name
  location                 = data.azurerm_resource_group.tfstate.location
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
