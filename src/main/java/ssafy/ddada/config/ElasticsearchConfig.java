package ssafy.ddada.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import ssafy.ddada.common.properties.ElasticsearchProperties;

import java.util.Arrays;

import org.apache.http.HttpHeaders;

@Slf4j
@Configuration
@Configurable
@RequiredArgsConstructor
@EnableElasticsearchAuditing
@EnableElasticsearchRepositories
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticsearchProperties elasticsearchProperties;

    @NotNull
    @Override
    public ClientConfiguration clientConfiguration() {
        log.info("Elasticsearch Client Configuration >>>> uris: {}", Arrays.toString(elasticsearchProperties.uris()));

        String encodedApiKey = elasticsearchProperties.apiKey(); // base64 인코딩된 API Key

        // spring-data-elasticsearch에서 제공하는 HttpHeaders 사용
        org.springframework.data.elasticsearch.support.HttpHeaders headers =
                new org.springframework.data.elasticsearch.support.HttpHeaders();

        headers.add(HttpHeaders.AUTHORIZATION, "ApiKey " + encodedApiKey);

        return ClientConfiguration.builder()
                .connectedTo(elasticsearchProperties.uris())
                .usingSsl()
                .withDefaultHeaders(headers)
                .build();
    }
}
