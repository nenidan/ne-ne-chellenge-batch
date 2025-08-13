package hello.batch.admin.job;

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

    private final StatisticsCalService statisticsCalService;
    private final StatisticsRedisRepository redisRepository;

    @Scheduled(cron = "0 0 1 1 * *") // 매월 1일 새벽 1시
    public void runMonthlyStatisticsUpdate() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth targetMonth = YearMonth.from(now);

        saveIfAbsent("challenge", targetMonth, () -> statisticsCalService.getChallengeStatistics(now));
        saveIfAbsent("payment", targetMonth, () -> statisticsCalService.getPaymentStatistics(now));
        saveIfAbsent("point", targetMonth, () -> statisticsCalService.getPointStatistics(now));
        saveIfAbsent("user", targetMonth, () -> statisticsCalService.getUserStatistics(now));
    }

    private <T> void saveIfAbsent(String type, YearMonth month, Supplier<T> supplier) {
        String key = type + ":" + month;
        String redisKey = "statistics:" + key;

        // 이미 값이 있는 경우 스킵
        if (redisRepository.exists(redisKey)) {
            return;
        }

        // 값 계산 후 저장
        T value = supplier.get();
        redisRepository.save(key, value);
    }
}
