package hello.batch.job.init.dto;

import java.time.LocalDate;

public class ChallengeDto {

    private final Long challengeId;

    private final LocalDate startAt;

    private final LocalDate dueAt;

    private final Integer participationFee;

    private final Integer totalDays;

    public ChallengeDto(Long challengeId, LocalDate startAt, LocalDate dueAt, Integer participationFee, Integer totalDays) {
        this.challengeId = challengeId;
        this.startAt = startAt;
        this.dueAt = dueAt;
        this.participationFee = participationFee;
        this.totalDays = totalDays;
    }
}
