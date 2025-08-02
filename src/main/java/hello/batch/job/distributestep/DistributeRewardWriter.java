package hello.batch.job.distributestep;

import hello.batch.job.client.PointClient;
import hello.batch.job.client.PointClientImpl;
import hello.batch.model.Reward;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 각 유저별로 실제 포인트를 증가시키고, 임시 테이블에서 해당 챌린지 정보를 삭제한다.
 */
public class DistributeRewardWriter implements ItemWriter<Reward> {

    private final JdbcTemplate jdbcTemplate;
    private final PointClient pointClient;

    public DistributeRewardWriter(JdbcTemplate jdbcTemplate, PointClient pointClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.pointClient = pointClient;
    }

    @Override
    public void write(Chunk<? extends Reward> chunk) throws Exception {
        chunk.getItems().forEach(r -> {
            distributeReward(r);
            clearTempTable(r.getChallengeId());
        });
    }

    private void distributeReward(Reward reward) {
        reward.getUserRewards().forEach(pointClient::addPoint);
    }

    private void clearTempTable(Long challengeId) {
        jdbcTemplate.update("DELETE FROM tmp_finished_challenge");
    }
}
