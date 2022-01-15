package com.spring.batch;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Component;

@Component
public class StorageUtils {

    public Storage getStorage(String env) throws Exception{
        return StorageOptions.newBuilder().
                setCredentials(ServiceAccountCredentials.fromStream(
                    getClass().getResourceAsStream("/service-account-"+env+".json"))).build()
                .getService();
    }
}
