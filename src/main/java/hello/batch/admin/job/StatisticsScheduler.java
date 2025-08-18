package hello.batch.admin.job;

import hello.batch.admin.model.dto.type.DomainType;
import hello.batch.admin.repository.StatisticTransactionRepository;
import hello.batch.admin.repository.StatisticsRedisRepository;
import hello.batch.admin.service.StatisticsCalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class StatisticsScheduler {

    private final StatisticsCalService calc;
    private final StatisticStore store;
    private final StatsPayloadMapper mapper;

    @Scheduled(cron = "0 0 1 1 * *") // 매월 1일 새벽 1시
    public void runMonthlyStatisticsUpdate() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth ym = YearMonth.from(now);

        try {
            store.upsert(DomainType.CHALLENGE, ym, mapper.toPayload(calc.getChallengeStatistics(now)));
        } catch (Exception e) { /* log.warn("challenge 실패", e); */ }

        try {
            store.upsert(DomainType.PAYMENT, ym, mapper.toPayload(calc.getPaymentStatistics(now)));
        } catch (Exception e) { /* log.warn("payment 실패", e); */ }

        try {
            store.upsert(DomainType.POINT, ym, mapper.toPayload(calc.getPointStatistics(now)));
        } catch (Exception e) { /* log.warn("point 실패", e); */ }

        try {
            store.upsert(DomainType.USER, ym, mapper.toPayload(calc.getUserStatistics(now)));
        } catch (Exception e) { /* log.warn("user 실패", e); */ }
    }

}
