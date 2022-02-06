
import datetime
from airflow import models
from airflow.providers.google.cloud.operators.dataflow import DataflowTemplatedJobStartOperator
from airflow.utils.dates import days_ago

bucket_path = "gs://dev-upstream-bucket/incoming"
project_id = "southern-branch-338317"
gce_zone = "us-east1-b"


default_args = {
    # Tell airflow to start one day ago, so that it runs as soon as you upload it
    "start_date": days_ago(1),
    "dataflow_default_options": {
        "project": project_id,
        # Set to your zone
        "zone": gce_zone,
        # This is a subfolder for storing temporary files, like the staged pipeline job.
        "tempLocation": bucket_path + "/staging/",
    },
}

with models.DAG(
    "composer_dataflow_dag",
    default_args=default_args,
    schedule_interval=datetime.timedelta(days=1),
) as dag:

    start_template_job = DataflowTemplatedJobStartOperator(
        task_id="dataflow_operator",
        template="gs://dev-upstream-bucket/templates/tx_report_job_template",
        parameters={
            "transactionFile": bucket_path + "/transactions.csv"
        },
    )

start_template_job
