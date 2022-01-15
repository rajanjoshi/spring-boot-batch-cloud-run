package com.spring.batch;

import java.io.IOException;
import java.io.FileInputStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobTargetOption;
import com.google.cloud.storage.Storage.PredefinedAcl;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.*;
import java.net.URI;
import com.google.api.gax.paging.Page;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.batch.item.ItemStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.env.Environment;
// import parent.spring.batch.StorageUtils;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public CoffeeRepository repository;

    @Autowired
    private Environment environment;

    // @Autowired
    // private StorageUtils storageUtils;

    @Bean
    public FlatFileItemReader<Coffee> reader() throws Exception{
        byte[] fileContents = null;
        String env = environment.getActiveProfiles()[0];
        try {	
            Bucket bucket = getStorage(env).get("spring-bucket-coffee-"+env+"");
            Page<Blob> blobs = bucket.list();
            for (Blob blob: blobs.getValues()) {
                fileContents = blob.getContent();
            }
        }catch(IllegalStateException e){
			throw new RuntimeException(e);
		}
        Path tempFile = Files.createTempFile("tempfiles", ".csv");
        Files.write(tempFile,fileContents);
        return new FlatFileItemReaderBuilder<Coffee>().name("coffeeItemReader")
            .resource(new PathResource(tempFile.toUri()))
            .delimited()
            .names(new String[] { "brand", "origin", "characteristics" })
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Coffee>() {{
                setTargetType(Coffee.class);
             }})
            .build();
    }

    @Bean
    public CoffeeItemProcessor processor() {
        return new CoffeeItemProcessor();
    }

    @Bean
    public ItemWriter<Coffee> writer() {
        return new RepositoryItemWriterBuilder<Coffee>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .end()
            .build();
    }

    @Bean
    public Step step1() throws Exception{
        return stepBuilderFactory.get("step1")
            .<Coffee, Coffee> chunk(2000)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .build();
    }

    private Storage getStorage(String env) throws Exception{
        return StorageOptions.newBuilder().
                   setCredentials(ServiceAccountCredentials.fromStream(
                    getClass().getResourceAsStream("/service-account-"+env+".json"))).build()
                   .getService();
       }

}
