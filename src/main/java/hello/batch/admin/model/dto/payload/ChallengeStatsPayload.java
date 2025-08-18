package hello.batch.admin.model.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeStatsPayload {
    private Map<String, Long> dailyParticipants;   // yyyy-MM-dd
    private Map<String, Long> monthlyParticipants; // yyyy-MM
    private double participationRate;
}
