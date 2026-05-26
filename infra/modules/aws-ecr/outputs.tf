output "repository_urls" {
  description = "Map of service short name to ECR repository URL."
  value       = { for k, r in aws_ecr_repository.this : k => r.repository_url }
}

output "repository_arns" {
  description = "Map of service short name to ECR repository ARN."
  value       = { for k, r in aws_ecr_repository.this : k => r.arn }
}

output "registry_id" {
  description = "AWS account ID hosting these repositories."
  value       = values(aws_ecr_repository.this)[0].registry_id
}
