
package com.spring.batch.utils;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

public class BigQueryUtils {

    public BigQuery bigQuery(String env, String projectId) throws Exception {
        return  BigQueryOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(
                        getClass().getResourceAsStream("/"+env+"-service-account.json")))
                    .setProjectId(projectId)
                    .build()
                    .getService();
    }
}
