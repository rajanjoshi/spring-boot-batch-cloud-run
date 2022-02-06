terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 3.53"
    }
  }
}

provider "google" {
  project = var.project
}

locals {
  service_folder = "service"
  service_name   = "terrraform-spring-batch"
}
