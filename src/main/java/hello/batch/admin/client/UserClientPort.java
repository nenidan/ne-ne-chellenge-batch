package hello.batch.admin.client;

import hello.batch.admin.model.dto.outer.PointDto;
import hello.batch.admin.model.dto.outer.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserClientPort {
    @Value("${external.base-url}")
    private String BASE_URL;

    private final RestClient restClient;

    private static final String ALL_PAYMENTS_ENDPOINT = "/internal/profiles";

    public List<UserDto> getAllUsers() {
        try {
            return restClient.get()
                    .uri(BASE_URL + ALL_PAYMENTS_ENDPOINT)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserDto>>() {
                    });
        }catch (RestClientException e) {
            log.error("외부 결제 목록 조회 실패", e);
            return List.of(); // 또는 null, 또는 custom 예외 throw
        }

    }
}
