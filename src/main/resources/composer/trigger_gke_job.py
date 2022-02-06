import datetime

from airflow import models
from airflow.providers.google.cloud.operators.kubernetes_engine import GKEStartPodOperator

yesterday = datetime.datetime.combine(
    datetime.datetime.today() - datetime.timedelta(1),
    datetime.datetime.min.time())

default_dag_args = {
    'start_date': yesterday,
    'retries': 1,
    'retry_delay': datetime.timedelta(seconds=10)
}

with models.DAG(
        'trigger_gke_job',
        schedule_interval=datetime.timedelta(days=1),
        default_args=default_dag_args) as dag:

    pod_task_xcom = GKEStartPodOperator(
        task_id="springbatch-job",
        project_id='southern-branch-338317',
        location='us-central1-c',
        cluster_name='dev-cluster',
        do_xcom_push=False,
        namespace="default",
        image="gcr.io/southern-branch-338317/spring-batch-postgres-gcs-bq17",
        cmds=["java", "-jar", "-Dspring.profiles.active=dev","springbatch.jar"],
        name="springbatch-job",
        is_delete_operator_pod=True,
        in_cluster=False,
    )

    pod_task_xcom
