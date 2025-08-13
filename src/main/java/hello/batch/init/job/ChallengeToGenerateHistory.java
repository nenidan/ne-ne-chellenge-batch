package hello.batch.init.job;
import java.time.LocalDate;

public class ChallengeToGenerateHistory {

    private final Long id;

    private final LocalDate startAt;

    private final LocalDate dueAt;

    private final Integer totalFee;

    private final Integer currentParticipantCount;

    public ChallengeToGenerateHistory(Long id, LocalDate startAt, LocalDate dueAt, Integer totalFee,
        Integer currentParticipantCount
    ) {
        this.id = id;
        this.startAt = startAt;
        this.dueAt = dueAt;
        this.totalFee = totalFee;
        this.currentParticipantCount = currentParticipantCount;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getStartAt() {
        return startAt;
    }

    public LocalDate getDueAt() {
        return dueAt;
    }

    public Integer getTotalFee() {
        return totalFee;
    }
    
    public Integer getCurrentParticipantCount() {
        return currentParticipantCount;
    }
}