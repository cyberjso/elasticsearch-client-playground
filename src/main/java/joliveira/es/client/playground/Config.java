package joliveira.es.client.playground;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@ComponentScan("joliveira.es.client.playground")
public class Config {

    private RestClient client;

    @Bean
    public RestClient lowLevelClient() {
        client =  RestClient
                .builder(new HttpHost("localhost", 9200, "http"))
                .setRequestConfigCallback(requestConfigBuilder
                        -> requestConfigBuilder
                        .setConnectTimeout(1000)
                        .setSocketTimeout(1000))
                .setMaxRetryTimeoutMillis(1000)
                .build();

        return client;
    }

    @PreDestroy
    public void cleanUp() {
        try {
            client.close();

        } catch (IOException e) {
            throw new RuntimeException("Error when closing connection with elasticsearch", e);

        }
    }

}
