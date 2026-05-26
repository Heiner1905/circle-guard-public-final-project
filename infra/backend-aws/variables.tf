variable "region" {
  description = "AWS region for the tfstate bucket and DynamoDB table."
  type        = string
  default     = "us-east-1"
}

variable "bucket_name" {
  description = "Globally unique S3 bucket name for Terraform remote state."
  type        = string
  default     = "cg-tfstate-h1905"

  validation {
    condition     = can(regex("^[a-z0-9.-]{3,63}$", var.bucket_name))
    error_message = "Bucket name must be 3-63 chars, lowercase letters, digits, dots and hyphens."
  }
}

variable "lock_table_name" {
  description = "DynamoDB table used by the S3 backend for state locking."
  type        = string
  default     = "cg-tfstate-locks"
}
