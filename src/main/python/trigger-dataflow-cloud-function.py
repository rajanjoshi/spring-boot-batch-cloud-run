
def startDataflowProcess(data, context):
	from googleapiclient.discovery import build
	project = "southern-branch-338317"
	job = 'txreport-1' + " " + str(data['timeCreated'])
	template = "gs://dev-upstream-bucket/templates/tx_report_job_template"
	inputFile = "gs://" + str(data['bucket']) + "/" + str(data['name'])
	#user defined parameters to pass to the dataflow pipeline job
	parameters = {
		'transactionFile': inputFile,
	}
	#tempLocation is the path on GCS to store temp files generated during the dataflow job
	environment = {'tempLocation': 'gs://dev-upstream-bucket/working'}

	service = build('dataflow', 'v1b3', cache_discovery=False)
	#below API is used when we want to pass the location of the dataflow job
	request = service.projects().locations().templates().launch(
		projectId=project,
		gcsPath=template,
		location='europe-west1',
		body={
			'jobName': job,
			'parameters': parameters,
			'environment':environment
		},
	)
	response = request.execute()
	print(str(response))
