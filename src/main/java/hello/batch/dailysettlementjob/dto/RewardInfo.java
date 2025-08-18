package hello.batch.dailysettlementjob.dto;

public class RewardInfo {

    private final Long challengeId;
    private final Long userId;
    private final Integer amount;

    public RewardInfo(Long challengeId, Long userId, Integer amount) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.amount = amount;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getAmount() {
        return amount;
    }
}
