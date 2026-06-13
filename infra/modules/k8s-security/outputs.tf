output "circleguard_namespace" {
  description = "Application namespace patched with PSA labels, ResourceQuota and LimitRange."
  value       = var.circleguard_namespace
}

output "psa_levels" {
  description = "Pod Security Admission levels effectively applied to the CircleGuard namespace."
  value = {
    enforce = var.psa_enforce_level
    audit   = var.psa_audit_level
    warn    = var.psa_warn_level
  }
}

output "cert_manager_namespace" {
  description = "Namespace where cert-manager runs (null when disabled)."
  value       = var.enable_cert_manager ? var.cert_manager_namespace : null
}

output "cluster_issuer_name" {
  description = "Name of the ClusterIssuer to reference in Ingress annotations (null when disabled). Example: cert-manager.io/cluster-issuer: <name>."
  value       = var.enable_cert_manager && var.enable_cluster_issuer ? var.cluster_issuer_name : null
}

output "network_policies_namespace" {
  description = "Namespace where Karold's helm chart renders the NetworkPolicies. Same as circleguard_namespace, exposed for clarity."
  value       = var.circleguard_namespace
}
