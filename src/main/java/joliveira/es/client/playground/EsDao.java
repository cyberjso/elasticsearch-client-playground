package joliveira.es.client.playground;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.TransportRequestOptions;
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
    private static final String INDEX = "person";

    private RestHighLevelClient client;

    @Autowired
    private RestClient lowLevelClient;

    @PostConstruct
    public void init() throws IOException {
        client = new RestHighLevelClient(lowLevelClient);

        createIndex( loadMappingContent("index-mapping.json"));
        createAliases(loadMappingContent("alias.json"));
    }


    public String save(Person person) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.field("age", person.getAge());
                builder.field("name", person.getName());
                builder.field("email", person.getEmail());
                builder.field("company_id", person.getCompanyId());
            }
            builder.endObject();

            IndexRequest  request =  new IndexRequest(INDEX);
            request
                    .id(person.getId())
                    .type("_doc") // https://discuss.elastic.co/t/validation-failed-because-of-missing-type-with-hl-rest-client-bulk-api/196060
                    .source(builder);

            IndexResponse response  = client.index(request);

            return response.getId();
        } catch (Exception e) {
            logger.error("Error when ingesting document", e);
            throw  new RuntimeException(e);
        }
    }

    private void createAliases(String mappingContent) {
        Header[] headers = { };

        HttpEntity entityCreate = new NStringEntity(mappingContent, ContentType.APPLICATION_JSON);
        try {
            lowLevelClient.performRequest("POST", "/_aliases", Collections.emptyMap(), entityCreate, headers);

        } catch (Exception e) {
            if (e.getMessage().contains("resource_already_exists_exception"))
                logger.info("l person already exists");
            else
                throw new RuntimeException("Error to create index person", e);
        }
    }

    private void createIndex(String mappingContent) {
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

    private String loadMappingContent(String resourceName) throws IOException {
        ClassLoader classLoader =   getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());

        return new String(Files.readAllBytes(file.toPath()));
    }

}
