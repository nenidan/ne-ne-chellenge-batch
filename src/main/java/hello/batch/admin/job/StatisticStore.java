package hello.batch.admin.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.batch.admin.model.StatisticData;
import hello.batch.admin.model.dto.type.DomainType;
import hello.batch.admin.repository.StatisticTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class StatisticStore {

    private final StatisticTransactionRepository statRepo; // statistics_data JPA
    private final ObjectMapper objectMapper;               // JavaTimeModule 등록된 것
    private final StringRedisTemplate stringRedisTemplate; // 문자열로 저장 추천

    @Transactional
    public void upsert(DomainType type, YearMonth ym, Object payload) {
        String payloadJson = toJson(payload);
        LocalDate anchor = ym.atDay(1);

        // DB upsert
        StatisticData entity = statRepo.findByTypeAndStatDate(type, anchor)
                .orElseGet(() -> StatisticData.of(type, ym, payloadJson));
        if (entity.getId() != null) {
            entity.updatePayload(payloadJson);
        }
        statRepo.save(entity);

        // Redis 캐시 (문자열로 저장 권장)
        String key = "statistics:" + type.name().toLowerCase() + ":" + ym;
        stringRedisTemplate.opsForValue().set(key, payloadJson);
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { throw new IllegalStateException("payload 직렬화 실패", e); }
    }
}
