package joliveira.es.client.playground;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class EsHealthChecker {
    private static Logger logger = LoggerFactory.getLogger(EsHealthChecker.class);

    @Autowired
    private RestClient client;

    @Autowired
    private ObjectMapper mapper;

    public boolean isHealthy() {

        try {
            Response response = client.performRequest("GET", "/_cluster/health");
            Integer statusCode = Optional.ofNullable(response.getStatusLine().getStatusCode()).orElse(401);

            String json =  EntityUtils.toString(response.getEntity());

            Map<String, String> status = mapper.readValue(json, new TypeReference<HashMap<String, String>>() {});

            if (!statusCode.equals(200)) {
                logger.warn(String.format("Status code returned was %s. Failing health check", statusCode));
                return false;
            }

            String clusterStatus  = status.getOrDefault("status", "RED");
            if (clusterStatus.equalsIgnoreCase("red")) {
                logger.warn("cluster status is red, failing. Failing health check");
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Error when fetching cluster  health status", e);

            return false;
        }

    }


}
