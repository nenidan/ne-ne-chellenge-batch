package hello.batch.dailysettlementjob.dto;

import java.util.Collections;
import java.util.Map;

public class Reward {

    private final Long challengeId;

    private final Map<Long, Integer> userRewards;

    public Reward(Long challengeId, Map<Long, Integer> userRewards) {
        this.challengeId = challengeId;
        this.userRewards = userRewards;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public Map<Long, Integer> getUserRewards() {
        return Collections.unmodifiableMap(userRewards);
    }
}
