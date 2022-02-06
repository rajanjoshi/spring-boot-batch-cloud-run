resource "google_container_node_pool" "dev-cluster_default-pool" {
  cluster            = "${google_container_cluster.dev-cluster.name}"
  initial_node_count = "1"
  location           = "us-central1-c"

  management {
    auto_repair  = "true"
    auto_upgrade = "true"
  }

  max_pods_per_node = "110"
  name              = "default-pool"

  node_config {
    disk_size_gb    = "100"
    disk_type       = "pd-standard"
    image_type      = "COS_CONTAINERD"
    local_ssd_count = "0"
    machine_type    = "n1-standard-1"

    metadata = {
      disable-legacy-endpoints = "true"
    }

    oauth_scopes    = ["https://www.googleapis.com/auth/logging.write", "https://www.googleapis.com/auth/cloud-platform", "https://www.googleapis.com/auth/monitoring", "https://www.googleapis.com/auth/devstorage.read_only"]
    preemptible     = "false"
    service_account = "dev-service-account@southern-branch-338317.iam.gserviceaccount.com"

    shielded_instance_config {
      enable_integrity_monitoring = "true"
      enable_secure_boot          = "false"
    }
  }

  node_count     = "1"
  node_locations = ["us-central1-c"]
  project        = "southern-branch-338317"

  upgrade_settings {
    max_surge       = "1"
    max_unavailable = "0"
  }

  version = "1.21.5-gke.1302"
}
