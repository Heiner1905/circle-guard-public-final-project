terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116"
    }
  }
}

resource "azurerm_kubernetes_cluster" "this" {
  name                = "aks-${var.name_prefix}"
  resource_group_name = var.resource_group_name
  location            = var.location
  dns_prefix          = "aks-${var.name_prefix}"
  kubernetes_version  = var.kubernetes_version

  # OIDC + Workload Identity enable pod-level Entra ID auth (no shared secrets).
  oidc_issuer_enabled               = true
  workload_identity_enabled         = true
  role_based_access_control_enabled = true

  default_node_pool {
    name                = "system"
    vm_size             = var.node_vm_size
    vnet_subnet_id      = var.aks_subnet_id
    enable_auto_scaling = false
    node_count          = 1
    # min_count            = var.node_min_count
    # max_count            = var.node_max_count
    os_disk_size_gb      = 30
    type                 = "VirtualMachineScaleSets"
    orchestrator_version = var.kubernetes_version
    tags                 = var.tags
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin    = "azure"
    network_policy    = "azure"
    load_balancer_sku = "standard"
    service_cidr      = var.service_cidr
    dns_service_ip    = var.dns_service_ip
  }

  # AZU-0041: si `api_server_authorized_ip_ranges` no está vacío, restringimos
  # el acceso al API server a esos CIDRs. Si está vacío, el bloque no se
  # renderiza y AKS queda con API público (suprimido en .trivyignore con TODO).
  dynamic "api_server_access_profile" {
    for_each = length(var.api_server_authorized_ip_ranges) > 0 ? [1] : []
    content {
      authorized_ip_ranges = var.api_server_authorized_ip_ranges
    }
  }

  tags = var.tags
}
