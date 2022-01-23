package com.spring.batch.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.spring.batch.model.Corpus;
import com.spring.batch.utils.BigQueryUtils;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamSupport;

public class CustomItemReader extends ItemStreamSupport implements ItemStreamReader<Corpus> {

    private String projectId;

    private String env;

    private Iterator<Corpus> recordIterator;

    @Override
    public void open(ExecutionContext executionContext) {
        String query = "SELECT corpus FROM `bigquery-public-data.samples.shakespeare` GROUP BY corpus;";
        List<Corpus> resultList = new ArrayList<>();
        try {
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).setPriority(QueryJobConfiguration.Priority.BATCH).build();
            TableResult result = new BigQueryUtils().bigQuery(env,projectId).query(queryConfig);
            resultList = StreamSupport.stream(result.getValues().spliterator(), false)
                                                .map(valueList -> new Corpus(valueList.get(0).getStringValue()))
                                                .collect(Collectors.toList());
        }catch (Exception e) {
            System.out.println("Big Query exception \n" + e.toString());
        }
        this.recordIterator = resultList.iterator();
    }

    @Override
    public Corpus read() {
        return recordIterator.hasNext() ? recordIterator.next() : null;
    }

    public CustomItemReader(String projectId, String env) {
        this.projectId = projectId;
        this.env = env;
    }


}
