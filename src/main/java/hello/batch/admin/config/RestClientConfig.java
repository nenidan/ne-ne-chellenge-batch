package hello.batch.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${external.base-url}")
    private String baseUrl;

    @Bean
    @Primary
    public RestClient restClient() {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(createRequestFactory())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public RestClient oauthRestClient() {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // 타임아웃 설정
    // RestClient 통신을 진행하면 타임아웃을 설정해야한다. 현재 응답을 5초로 설정해놓아서,
    // 클라이언트 통신에서 5초 이상 응답이 없을 시 예외를 던진다.
    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));   // 연결: 3초
        factory.setReadTimeout(Duration.ofSeconds(5));     // 응답: 5초
        return factory;
    }
}
