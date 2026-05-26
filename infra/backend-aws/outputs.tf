output "bucket_name" {
  description = "S3 bucket holding *.tfstate files."
  value       = aws_s3_bucket.tfstate.id
}

output "lock_table_name" {
  description = "DynamoDB table used for state locking."
  value       = aws_dynamodb_table.tfstate_locks.name
}

output "region" {
  description = "AWS region of the backend resources."
  value       = var.region
}

output "backend_config_snippet" {
  description = "Copy/paste-ready backend config snippet for environments."
  value       = <<-EOT
    terraform {
      backend "s3" {
        bucket         = "${aws_s3_bucket.tfstate.id}"
        key            = "<env>.terraform.tfstate"
        region         = "${var.region}"
        dynamodb_table = "${aws_dynamodb_table.tfstate_locks.name}"
        encrypt        = true
      }
    }
  EOT
}
