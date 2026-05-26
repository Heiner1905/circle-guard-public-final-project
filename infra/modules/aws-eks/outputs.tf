output "cluster_id" {
  description = "EKS cluster ID."
  value       = aws_eks_cluster.this.id
}

output "cluster_name" {
  description = "EKS cluster name."
  value       = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  description = "Kubernetes API endpoint."
  value       = aws_eks_cluster.this.endpoint
}

output "cluster_certificate_authority_data" {
  description = "Base64-encoded CA cert for the cluster."
  value       = aws_eks_cluster.this.certificate_authority[0].data
}

output "cluster_role_arn" {
  description = "ARN of the IAM role assumed by the EKS control plane."
  value       = aws_iam_role.cluster.arn
}

output "node_role_arn" {
  description = "ARN of the IAM role assumed by the worker nodes."
  value       = aws_iam_role.nodes.arn
}

output "node_group_name" {
  description = "Name of the default managed node group."
  value       = aws_eks_node_group.default.node_group_name
}
