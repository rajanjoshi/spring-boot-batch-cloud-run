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

    @Value("classpath:service-account.json")
    private Resource credentials;

    @GetMapping("/")
    String hello() throws IOException{
        return "Hello World";
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
                   new FileInputStream(credentials.getFile()))).build()
                   .getService();
       }

}
