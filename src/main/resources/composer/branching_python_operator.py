import random
import datetime
import requests
from airflow import models
from airflow.operators import bash_operator
from airflow.operators import python_operator
from airflow.operators import dummy_operator

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
        api_url1 = "https://springbatch-3bvkgbgkrq-uc.a.run.app/"
        response1 = requests.get(api_url1)
        return str(response1.content)

    def loadCsvToDb():
        api_url2 = "https://springbatch-3bvkgbgkrq-uc.a.run.app/triggerJob"
        response2 = requests.get(api_url2)
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

    branching = python_operator.BranchPythonOperator(
        task_id='branching',
        python_callable=makeBranchChoice
    )

    run_this_first >> branching
          
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

    branching >> [hello_world, load_csv_to_db] >> bash_greeting
