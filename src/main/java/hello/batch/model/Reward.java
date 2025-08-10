package hello.batch.model;

import java.util.Collections;
import java.util.Map;

public class Reward {

    private final Long challengeId;

    private final Map<Long, Integer> userRewards;

    private final Map<Long, Long> userPointWallet;

    public Reward(Long challengeId, Map<Long, Integer> userRewards, Map<Long, Long> userPointWallet) {
        this.challengeId = challengeId;
        this.userRewards = userRewards;
        this.userPointWallet = userPointWallet;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public Map<Long, Integer> getUserRewards() {
        return Collections.unmodifiableMap(userRewards);
    }

    public Map<Long, Long> getUserPointWallet() {
        return Collections.unmodifiableMap(userPointWallet);
    }
}
