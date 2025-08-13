package hello.batch.dto;

public class ChallengeResult {

    private final Long challengeId;
    private final Integer totalFee;

    private final Integer totalDays;

    public ChallengeResult(Long challengeId, Integer totalFee, Integer totalDays) {
        this.challengeId = challengeId;
        this.totalFee = totalFee;
        this.totalDays = totalDays;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public Integer getTotalDays() {
        return totalDays;
    }
}
