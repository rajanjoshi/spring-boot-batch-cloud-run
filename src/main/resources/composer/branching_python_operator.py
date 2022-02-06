import random
import datetime
import requests
import google.auth
import google.auth.transport.requests
from google.oauth2 import service_account
from google.auth.transport.requests import AuthorizedSession
from airflow import models
from airflow.operators import bash_operator
from airflow.operators import python_operator
from airflow.operators import dummy_operator
from airflow.utils import trigger_rule

yesterday = datetime.datetime.combine(
    datetime.datetime.today() - datetime.timedelta(1),
    datetime.datetime.min.time())   

default_dag_args = {
    'start_date': yesterday
}

with models.DAG(
        'branching_python_operator',
        schedule_interval=datetime.timedelta(days=1),
        default_args=default_dag_args) as dag:
      

    def greeting():
        service_url = "https://springbatch-dev-7apjdjndna-uc.a.run.app/"
        key_file = '/home/airflow/gcs/data/composer_account.json'

        credentials = service_account.IDTokenCredentials.from_service_account_file(
            key_file, target_audience=service_url)
        request = google.auth.transport.requests.Request()
        credentials.refresh(request)
        token = credentials.token
        print(token)
        response1 = requests.get(service_url,headers={'Authorization': 'Bearer '+token})
        return str(response1.content)


    def loadCsvToDb():
        service_url = "https://springbatch-dev-7apjdjndna-uc.a.run.app/triggerGcstoDbJob"
        key_file = '/home/airflow/gcs/data/composer_account.json'

        credentials = service_account.IDTokenCredentials.from_service_account_file(
            key_file, target_audience=service_url)
        request = google.auth.transport.requests.Request()
        credentials.refresh(request)
        token = credentials.token
        print(token)
        response2 = requests.get(service_url,headers={'Authorization': 'Bearer '+token})
        return str(response2.content)    

    def makeBranchChoice():
        x = random.randint(1, 5)
        if(x <= 2):
            return 'hello_world'
        else:
            return 'load_csv_to_db'  

    run_this_first = dummy_operator.DummyOperator(
        task_id='run_this_first'
    )

    set_min_instance_one = bash_operator.BashOperator(
        task_id='set_min_instance_one',
        bash_command='gcloud run services update springbatch-dev --min-instances 1 --region=us-central1 --no-cpu-throttling ',
    )

    branching = python_operator.BranchPythonOperator(
        task_id='branching',
        python_callable=makeBranchChoice
    )

    run_this_first >> set_min_instance_one
          
    hello_world = python_operator.PythonOperator(
        task_id='hello_world',
        python_callable=greeting)

    load_csv_to_db = python_operator.PythonOperator(
        task_id='load_csv_to_db',
        python_callable=loadCsvToDb)

    bash_greeting = bash_operator.BashOperator(
        task_id='bye_bash',
        bash_command='echo Goodbye! Hope to see you soon.',
        trigger_rule='one_success'
    )



    set_min_instance_zero = bash_operator.BashOperator(
        task_id='set_min_instance_zero',
        bash_command='gcloud run services update springbatch-dev --min-instances 0 --region=us-central1 --cpu-throttling ',
        trigger_rule=trigger_rule.TriggerRule.ALL_DONE
    )

    set_min_instance_one >> branching >> [hello_world, load_csv_to_db] >> set_min_instance_zero >> bash_greeting
