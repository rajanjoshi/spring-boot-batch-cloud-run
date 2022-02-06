variable "project" {
  type        = string
  description = "Google Cloud Platform Project ID"
  default = "powerful-vine-329211"
}

variable "region" {
  default = "us-central1"
  type    = string
}

variable "cluster" {
  default = "dev-cluster"
  type    = string
}
