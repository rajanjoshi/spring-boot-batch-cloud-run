resource "google_cloudfunctions_function" "dataflow-trigger-function" {
  available_memory_mb = "128"
  entry_point         = "startDataflowProcess"
  event_trigger {
    event_type = "google.storage.object.finalize"
    failure_policy {
      retry = "false"
    }
    resource = "projects/_/buckets/dev-upstream-bucket"
  }
  ingress_settings = "ALLOW_INTERNAL_ONLY"
  max_instances         = "30"
  min_instances         = "0"
  name                  = "dataflow-trigger-function"
  project               = google_cloud_run_service.terraform-spring-batch.project
  region                = google_cloud_run_service.terraform-spring-batch.location
  runtime               = "python37"
  service_account_email = "dev-service-account@southern-branch-338317.iam.gserviceaccount.com"
  timeout               = "60"
}
