package joliveira.es.client.playground;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

@Component
public class EsDao {
    private static Logger logger = LoggerFactory.getLogger(EsDao.class);

    private RestHighLevelClient client;

    @Autowired
    private RestClient lowLevelClient;

    @PostConstruct
    public void init() throws IOException {
        client = new RestHighLevelClient(lowLevelClient);

        ClassLoader classLoader =   getClass().getClassLoader();
        File file = new File(classLoader.getResource("index-mapping.json").getFile());
        String mappingContent = new String(Files.readAllBytes(file.toPath()));

        Header[] headers = { };

        HttpEntity entityCreate = new NStringEntity(mappingContent, ContentType.APPLICATION_JSON);

        try {
            lowLevelClient.performRequest("PUT", "/person", Collections.emptyMap(), entityCreate, headers);
        } catch (Exception e) {
            if (e.getMessage().contains("resource_already_exists_exception"))
                logger.info("Index person already exists");
            else
                throw new RuntimeException("Error to create index person", e);
        }
    }



}
