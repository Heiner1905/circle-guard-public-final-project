locals {
  environment = "dev"
  name_prefix = "circleguard-${local.environment}"

  common_tags = merge(var.extra_tags, {
    project     = "circleguard"
    managedBy   = "terraform"
    environment = local.environment
  })
}

provider "azurerm" {
  features {}
}

provider "aws" {
  region = var.aws_region
}

# ----------------------------------------------------------------------------
# Azure: resource group + network + AKS + ACR
# ----------------------------------------------------------------------------

resource "azurerm_resource_group" "this" {
  name     = "rg-${local.name_prefix}"
  location = var.azure_location
  tags     = local.common_tags
}

module "network" {
  source = "../../modules/azure-network"

  name_prefix         = local.name_prefix
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  vnet_cidr           = var.vnet_cidr
  aks_subnet_cidr     = var.aks_subnet_cidr
  tags                = local.common_tags
}

module "aks" {
  source = "../../modules/azure-aks"

  name_prefix         = local.name_prefix
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  aks_subnet_id       = module.network.aks_subnet_id
  kubernetes_version  = var.kubernetes_version
  node_vm_size        = var.node_vm_size
  node_min_count      = var.node_min_count
  node_max_count      = var.node_max_count
  service_cidr        = var.service_cidr
  dns_service_ip      = var.dns_service_ip
  tags                = local.common_tags
}

module "acr" {
  source = "../../modules/azure-acr"

  acr_name                       = var.acr_name
  resource_group_name            = azurerm_resource_group.this.name
  location                       = azurerm_resource_group.this.location
  sku                            = var.acr_sku
  aks_kubelet_identity_object_id = module.aks.kubelet_identity_object_id
  tags                           = local.common_tags
}

# ----------------------------------------------------------------------------
# Kubernetes / Helm providers — fed from the AKS module outputs.
# Using yamldecode + base64decode of the raw kubeconfig avoids relying on
# kube_admin_config_raw (only emitted when Entra ID RBAC is enabled).
# ----------------------------------------------------------------------------

locals {
  kube_config = yamldecode(module.aks.kube_config_raw)
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

module "middleware" {
  source = "../../modules/k8s-middleware"

  namespace        = var.middleware_namespace
  persistence_size = var.middleware_persistence_size

  postgres_username   = var.postgres_username
  postgres_password   = var.postgres_password
  postgres_database   = var.postgres_database
  neo4j_password      = var.neo4j_password
  redis_password      = var.redis_password
  ldap_admin_user     = var.ldap_admin_user
  ldap_admin_password = var.ldap_admin_password
  ldap_domain         = var.ldap_domain

  depends_on = [module.aks]
}

# ----------------------------------------------------------------------------
# Observability stack (kube-prometheus-stack + Grafana dashboards).
# Gated by enable_observability so individual envs can opt out.
# ----------------------------------------------------------------------------

module "observability" {
  source = "../../modules/k8s-observability"
  count  = var.enable_observability ? 1 : 0

  namespace                = var.observability_namespace
  grafana_admin_user       = var.grafana_admin_user
  grafana_admin_password   = var.grafana_admin_password
  prometheus_retention     = var.prometheus_retention
  prometheus_storage_size  = var.prometheus_storage_size
  grafana_persistence_size = var.grafana_persistence_size
  dashboards_path          = var.dashboards_path

  depends_on = [module.aks]
}

# ----------------------------------------------------------------------------
# Logging stack (Loki + Promtail, optional ECK).
# Reuses the observability namespace so Grafana can read Loki via an
# in-cluster datasource without cross-namespace token plumbing.
# ----------------------------------------------------------------------------

module "logging" {
  source = "../../modules/k8s-logging"
  count  = var.enable_logging ? 1 : 0

  namespace            = var.observability_namespace
  create_namespace     = false
  loki_retention_hours = var.loki_retention_hours
  loki_storage_size    = var.loki_storage_size
  enable_elk           = var.enable_elk

  depends_on = [module.observability]
}

# ----------------------------------------------------------------------------
# AWS multi-cloud (optional, gated by enable_aws). Modules use count so the
# whole AWS stack is skipped when the flag is false.
# ----------------------------------------------------------------------------

module "aws_network" {
  source = "../../modules/aws-network"
  count  = var.enable_aws ? 1 : 0

  name_prefix      = local.name_prefix
  vpc_cidr         = var.aws_vpc_cidr
  cluster_name_tag = "eks-${local.name_prefix}"
  tags             = local.common_tags
}

module "aws_eks" {
  source = "../../modules/aws-eks"
  count  = var.enable_aws ? 1 : 0

  name_prefix        = local.name_prefix
  cluster_name       = "eks-${local.name_prefix}"
  kubernetes_version = var.aws_kubernetes_version
  subnet_ids         = module.aws_network[0].public_subnet_ids
  node_instance_type = var.aws_node_instance_type
  node_min_size      = var.aws_node_min_size
  node_max_size      = var.aws_node_max_size
  node_desired_size  = var.aws_node_desired_size
  tags               = local.common_tags
}

module "aws_ecr" {
  source = "../../modules/aws-ecr"
  count  = var.enable_aws ? 1 : 0

  repo_prefix = "circleguard"
  tags        = local.common_tags
}
