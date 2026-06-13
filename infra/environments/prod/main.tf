locals {
  environment = "prod"
  name_prefix = "circleguard-${local.environment}"

  common_tags = merge(var.extra_tags, {
    project     = "circleguard"
    managedBy   = "terraform"
    environment = local.environment
  })
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
}

# ----------------------------------------------------------------------------
# Azure: usar recursos existentes de dev
# ----------------------------------------------------------------------------

data "azurerm_resource_group" "this" {
  name = "CircleGuard"
}

data "azurerm_kubernetes_cluster" "this" {
  name                = "aks-circleguard-dev"
  resource_group_name = data.azurerm_resource_group.this.name
}

data "azurerm_container_registry" "this" {
  name                = "acrcircleguarddev2"
  resource_group_name = data.azurerm_resource_group.this.name
}

# ----------------------------------------------------------------------------
# Kubernetes / Helm providers
# ----------------------------------------------------------------------------

locals {
  kube_config = yamldecode(data.azurerm_kubernetes_cluster.this.kube_config_raw)
  kube_host   = local.kube_config.clusters[0].cluster.server
  kube_ca     = base64decode(local.kube_config.clusters[0].cluster["certificate-authority-data"])
  kube_cert   = base64decode(local.kube_config.users[0].user["client-certificate-data"])
  kube_key    = base64decode(local.kube_config.users[0].user["client-key-data"])
}

provider "kubernetes" {
  host                   = local.kube_host
  cluster_ca_certificate = local.kube_ca
  client_certificate     = local.kube_cert
  client_key             = local.kube_key
}

provider "helm" {
  kubernetes {
    host                   = local.kube_host
    cluster_ca_certificate = local.kube_ca
    client_certificate     = local.kube_cert
    client_key             = local.kube_key
  }
}

# ----------------------------------------------------------------------------
# TODOS LOS MÓDULOS DESHABILITADOS - Los recursos ya existen en dev
# ----------------------------------------------------------------------------

# module "middleware" {
#   source = "../../modules/k8s-middleware"
#   ...
# }

# module "observability" {
#   ...
# }

# module "logging" {
#   ...
# }

# module "tracing" {
#   ...
# }

# module "security" {
#   ...
# }