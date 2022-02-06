# The Cloud Run service1
resource "google_cloud_run_service" "terraform-spring-batch" {
  name                       = local.service_name
  location                   = var.region
  autogenerate_revision_name = true

  template {
    spec {
      service_account_name = "dev-service-acount@southern-branch-338317.iam.gserviceaccount.com"
      containers {
        image = "gcr.io/powerful-vine-329211/spring-batch-postgres-gcs-bq10"
      }
    }
  }
  traffic {
    percent         = 100
    latest_revision = true
  }

  depends_on = [google_project_service.run]
}

# Set service publicg
data "google_iam_policy" "noauth" {
  binding {

    role = "roles/run.invoker"
    members = [
     serviceaccount:dev-composer@southern-branch-338317.iam.gserviceaccount.com,
    ]
  }
}

resource "google_cloud_run_service_iam_policy" "noauth" {
  location = google_cloud_run_service.terraform-spring-batch.location
  project  = google_cloud_run_service.terraform-spring-batch.project
  service  = google_cloud_run_service.terraform-spring-batch.name
  policy_data = data.google_iam_policy.noauth.policy_data
  depends_on  = [google_cloud_run_service.terraform-spring-batch]
}
