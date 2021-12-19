package com.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobTargetOption;
import com.google.cloud.storage.Storage.PredefinedAcl;
import com.google.cloud.storage.StorageOptions;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.CsvOptions;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.TableId;
import java.util.stream.StreamSupport;

@RestController
public class HelloController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("gs://spring-bucket-coffee/coffee-list.CSV")
    private Resource gcsFile;

    @Value("classpath:sample.xml")
    private Resource sampleXml;

    @GetMapping("/")
    String hello() throws IOException{
        return "Hello World";
    }

    @GetMapping("/readFromBQ")
    String simpleBigQuery() throws IOException{
        String query = "SELECT corpus FROM `bigquery-public-data.samples.shakespeare` GROUP BY corpus;";
        List<String> resultList = new ArrayList();
        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            BigQuery bigquery =  BigQueryOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                    getClass().getResourceAsStream("/service-account.json")))
                .setProjectId("powerful-vine-329211")
                .build()
                .getService();
      
            // Create the query job.
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
      
            // Execute the query.
            TableResult result = bigquery.query(queryConfig);
            resultList = StreamSupport.stream(result.getValues().spliterator(), false)
			.map(valueList -> valueList.get(0).getStringValue())
			.collect(Collectors.toList());

            System.out.println("Query ran successfully");
          } catch (BigQueryException | InterruptedException e) {
            System.out.println("Query did not run \n" + e.toString());
          }
        return resultList.toString();
    }

    @GetMapping("/loadFromGcsToBigQuery")
    String loadFromGcsToBigQuery() throws IOException{
        String result=null;
        String datasetName = "marketplace";
        String tableName = "TechCrunch";
        String sourceUri = "gs://spring-bucket-coffee/coffee-list.CSV";
        Schema schema = Schema.of(
                        Field.of("column1", StandardSQLTypeName.STRING),
                        Field.of("column2", StandardSQLTypeName.STRING),
                        Field.of("column3", StandardSQLTypeName.STRING));
        try {

            BigQuery bigquery =  BigQueryOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                    getClass().getResourceAsStream("/service-account.json")))
                .setProjectId("powerful-vine-329211")
                .build()
                .getService();
        
            // Skip header row in the file.
            CsvOptions csvOptions = CsvOptions.newBuilder().setSkipLeadingRows(1).build();
        
            TableId tableId = TableId.of(datasetName, tableName);
            LoadJobConfiguration loadConfig =
                LoadJobConfiguration.newBuilder(tableId, sourceUri, csvOptions).setSchema(schema).build();
        
            // Load data from a GCS CSV file into the table
            com.google.cloud.bigquery.Job job = bigquery.create(JobInfo.of(loadConfig));
            // Blocks until this load table job completes its execution, either failing or succeeding.
            job = job.waitFor();
            if (job.isDone()) {
                result = "CSV from GCS successfully added during load append job";
                System.out.println("CSV from GCS successfully added during load append job");
            } else {
                result = "BigQuery was unable to load into the table due to an error:"+ job.getStatus().getError();
                System.out.println(result);
            }
            } catch (BigQueryException | InterruptedException e) {
                result = "Column not added during load append \n"+ e.toString();
                System.out.println(result);
            }
        return result;    

    }

    @GetMapping(value = "/exportCsvFromGcs", produces = "text/csv")
    public ResponseEntity<Resource> exportCSV() throws IOException{
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + gcsFile.getFilename());
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        InputStreamResource fileInputStream = new InputStreamResource(gcsFile.getInputStream());
        return new ResponseEntity<>(
            fileInputStream,
            headers,
            HttpStatus.OK
        );
    }

    @RequestMapping("/triggerJob")
    public List<String> triggerJob() throws Exception{
       JobParameters jobParameters =
                       new JobParametersBuilder()
                       .addLong("time",System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(job, jobParameters);

        return this.jdbcTemplate.queryForList("SELECT * FROM coffee").stream()
				.map((m) -> m.values().toString())
				.collect(Collectors.toList());
    }

    @RequestMapping("/writeFileToGCS")
    public String writeFile() throws Exception{
        File file = new File(sampleXml.getURI());
        String hourMinute = String.valueOf(System.currentTimeMillis());
        try {			
			    BlobInfo blobInfo = getStorage().create(
				BlobInfo.newBuilder("spring-bucket-coffee", hourMinute+"_"+file.getName()).build(), 
				Files.readAllBytes(file.toPath()), 
				BlobTargetOption.predefinedAcl(PredefinedAcl.PUBLIC_READ) 
			);
			return blobInfo.getMediaLink(); 
		}catch(IllegalStateException e){
			throw new RuntimeException(e);
		}
    }

    private Storage getStorage() throws Exception{
        return StorageOptions.newBuilder().
                   setCredentials(ServiceAccountCredentials.fromStream(
                    getClass().getResourceAsStream("/service-account.json"))).build()
                   .getService();
       }

}
