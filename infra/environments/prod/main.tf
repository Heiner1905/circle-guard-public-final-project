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

  name_prefix                     = local.name_prefix
  resource_group_name             = azurerm_resource_group.this.name
  location                        = azurerm_resource_group.this.location
  aks_subnet_id                   = module.network.aks_subnet_id
  kubernetes_version              = var.kubernetes_version
  node_vm_size                    = var.node_vm_size
  node_min_count                  = var.node_min_count
  node_max_count                  = var.node_max_count
  service_cidr                    = var.service_cidr
  dns_service_ip                  = var.dns_service_ip
  api_server_authorized_ip_ranges = var.api_server_authorized_ip_ranges
  tags                            = local.common_tags
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
# ----------------------------------------------------------------------------

module "logging" {
  source = "../../modules/k8s-logging"
  count  = var.enable_logging ? 1 : 0

  namespace              = var.observability_namespace
  create_namespace       = false
  loki_retention_hours   = var.loki_retention_hours
  loki_storage_size      = var.loki_storage_size
  enable_elk             = var.enable_elk
  tracing_datasource_uid = var.enable_tracing ? "jaeger" : ""

  depends_on = [module.observability]
}

# ----------------------------------------------------------------------------
# Tracing stack (Jaeger all-in-one + Alertmanager rules via PrometheusRule).
# ----------------------------------------------------------------------------

module "tracing" {
  source = "../../modules/k8s-tracing"
  count  = var.enable_tracing ? 1 : 0

  namespace                = var.observability_namespace
  create_namespace         = false
  jaeger_memory_max_traces = var.jaeger_memory_max_traces
  enable_alerts            = var.enable_alerts

  depends_on = [module.observability]
}

# ----------------------------------------------------------------------------
# Security stack (US-15) - Pod Security Admission labels on the application
# namespace, ResourceQuota + LimitRange, cert-manager and a ClusterIssuer
# (ACME / Let's Encrypt prod by default - real certificates).
#
# Apply order note: this module patches labels on the `circleguard` namespace,
# which is owned by Karold's helm chart. Run terraform once to create the
# cluster, then `helm install circleguard helm/circleguard ...`, then re-run
# terraform so the kubernetes_labels resource patches the namespace in place.
# ----------------------------------------------------------------------------

module "security" {
  source = "../../modules/k8s-security"
  count  = var.enable_security ? 1 : 0

  circleguard_namespace = var.application_namespace

  psa_enforce_level = var.psa_enforce_level
  psa_audit_level   = var.psa_audit_level
  psa_warn_level    = var.psa_warn_level

  enable_resource_quota          = var.enable_resource_quota
  resource_quota_cpu_requests    = var.resource_quota_cpu_requests
  resource_quota_memory_requests = var.resource_quota_memory_requests
  resource_quota_cpu_limits      = var.resource_quota_cpu_limits
  resource_quota_memory_limits   = var.resource_quota_memory_limits
  resource_quota_pods            = var.resource_quota_pods

  enable_cert_manager        = var.enable_cert_manager
  cert_manager_chart_version = var.cert_manager_chart_version

  enable_cluster_issuer             = var.enable_cluster_issuer
  cluster_issuer_kind               = var.cluster_issuer_kind
  cluster_issuer_acme_email         = var.cluster_issuer_acme_email
  cluster_issuer_acme_server        = var.cluster_issuer_acme_server
  cluster_issuer_acme_ingress_class = var.cluster_issuer_acme_ingress_class

  depends_on = [module.observability]
}