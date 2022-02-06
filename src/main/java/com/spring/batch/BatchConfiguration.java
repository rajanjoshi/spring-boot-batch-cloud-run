package com.spring.batch;
import com.spring.batch.model.*;
import com.spring.batch.reader.CustomItemReader;
import com.spring.batch.utils.StorageUtils;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;


import com.google.cloud.storage.*;
import com.google.api.gax.paging.Page;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.env.Environment;
// import parent.spring.batch.StorageUtils;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Value("${projectId}")
    private String projectId;
	
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public CoffeeRepository repository;

    @Autowired
    private Environment environment;

    @Autowired
    private StorageUtils storageUtils;

    @Bean
    public FlatFileItemReader<Coffee> reader() throws Exception{
        byte[] fileContents = null;
        String env = environment.getActiveProfiles()[0];
        try {	
            Bucket bucket = storageUtils.getStorage(env).get(env+"-upstream-bucket");
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
    public CustomItemReader readerBQ(){
        String env = environment.getActiveProfiles()[0];
        return new CustomItemReader(projectId, env);
    }

    @Bean
    public CoffeeItemProcessor processor() {
        return new CoffeeItemProcessor();
    }

    @Bean
    public BQItemProcessor bqItemProcessor() {
        return new BQItemProcessor();
    }

    @Bean
    public ItemWriter<Coffee> writer() {
        return new RepositoryItemWriterBuilder<Coffee>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) throws Exception {
        return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .end()
            .build();
    }

    @Bean
    public Job bigQueryReadJob(Step bigQueryReadStep) throws Exception {
        return jobBuilderFactory.get("bigQueryReadJob")
            .incrementer(new RunIdIncrementer())
            .flow(bigQueryReadStep)
            .end()
            .build();
    }

    @Bean
    @Qualifier("step1")
    public Step step1() throws Exception{
        return stepBuilderFactory.get("step1")
            .<Coffee, Coffee> chunk(5)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .build();
    }

    @Bean
    @Qualifier("bigQueryReadStep")
    public Step bigQueryReadStep() throws Exception{
        return stepBuilderFactory.get("bigQueryReadStep")
            .<Corpus, Coffee> chunk(20)
            .reader(readerBQ())
            .processor(bqItemProcessor())
            .writer(writer())
            .build();
    }

}
