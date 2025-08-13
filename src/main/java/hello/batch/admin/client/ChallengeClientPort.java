package hello.batch.admin.client;

import hello.batch.admin.model.dto.outer.ChallengeDto;
import hello.batch.admin.model.dto.outer.ChallengeUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeClientPort {
    @Value("${external.base-url}")
    private String BASE_URL;

    private final RestClient restClient;

    private static final String ALL_CHALLENGES_ENDPOINT = "/internal/statistics/challenges";
    private static final String ALL_CHALLENGE_USERS_ENDPOINT = "/internal/statistics/participants";

    public List<ChallengeDto> getAllChallenges() {
        try {
            return restClient.get()
                    .uri(BASE_URL + ALL_CHALLENGES_ENDPOINT)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ChallengeDto>>() {
                    });
        }catch (RestClientException e) {
            log.error("외부 챌린지 목록 조회 실패", e);
            return List.of(); // 또는 null, 또는 custom 예외 throw
        }

    }

    public List<ChallengeUserDto> getAllChallengeUsers() {
        try {
            return restClient.get()
                    .uri(BASE_URL + ALL_CHALLENGE_USERS_ENDPOINT)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ChallengeUserDto>>() {
                    });
        }catch (RestClientException e) {
            log.error("외부 챌린지 유저목록 조회 실패", e);
            return List.of(); // 또는 null, 또는 custom 예외 throw
        }
    }
}
