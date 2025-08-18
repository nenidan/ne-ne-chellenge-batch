package hello.batch.admin.job;

// batch

import hello.batch.admin.model.dto.ChallengeStatisticsResponse;
import hello.batch.admin.model.dto.PaymentStatisticsResponse;
import hello.batch.admin.model.dto.PointStatisticsResponse;
import hello.batch.admin.model.dto.UserStatisticsResponse;
import hello.batch.admin.model.dto.payload.ChallengeStatsPayload;
import hello.batch.admin.model.dto.payload.PaymentStatsPayload;
import hello.batch.admin.model.dto.payload.PointStatsPayload;
import hello.batch.admin.model.dto.payload.UserStatsPayload;
import hello.batch.admin.model.dto.type.ChallengeStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatsPayloadMapper {

    public ChallengeStatsPayload toPayload(ChallengeStatisticsResponse r) {
        Map<String, Long> daily = r.getDailyParticipants().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));

        Map<String, Long> monthly = r.getMonthlyParticipants().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));

        return new ChallengeStatsPayload(daily, monthly, r.getParticipationRate());
    }

    public PaymentStatsPayload toPayload(PaymentStatisticsResponse r) {
        return new PaymentStatsPayload(r.getCount(), r.getAvgAmount());
    }

    public PointStatsPayload toPayload(PointStatisticsResponse r) {
        return new PointStatsPayload(r.getReasonRate(), r.getCnt(), r.getReason());
    }

    public UserStatsPayload toPayload(UserStatisticsResponse r) {
        return new UserStatsPayload(r.getNewUsersCount(), r.getTotalUsersCount());
    }
}
