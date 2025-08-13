package hello.batch.job.calculaterewardstep;

import hello.batch.dto.Reward;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RewardInfoWriter implements ItemWriter<Reward> {

    private final JdbcTemplate jdbcTemplate;

    public RewardInfoWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 각 사용자가 어떤 챌린지에 참여해서 얼마만큼의 보상을 얻었는지 tmp_reward_info 테이블에 (user_id, challenge_id, amount)를 저장하고 </br>
     * tmp_finished_challenge 테이블에서 해당하는 row를 지운다.
     * @param chunk of items to be written. Must not be {@code null}.
     * @throws Exception
     */
    @Override
    public void write(Chunk<? extends Reward> chunk) throws Exception {
        chunk.getItems().forEach(this::writeRewardInfo);

        List<Long> challengeIdsToDelete = chunk.getItems().stream()
            .map(Reward::getChallengeId)
            .toList();

        clearTempTableWithIn(challengeIdsToDelete);
    }

    private void writeRewardInfo(Reward reward) {
        String sql = "INSERT INTO tmp_reward_info(user_id, challenge_id, amount) VALUES (?, ?, ?)";
        Map<Long, Integer> userRewards = reward.getUserRewards();
        List<Map.Entry<Long, Integer>> entries = new ArrayList<>(userRewards.entrySet());

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map.Entry<Long, Integer> entry = entries.get(i);
                ps.setLong(1, entry.getKey());
                ps.setLong(2, reward.getChallengeId());
                ps.setInt(3, entry.getValue());
            }

            @Override
            public int getBatchSize() {
                return entries.size();
            }
        });
    }

    private void clearTempTableWithIn(List<Long> challengeIds) {
        String placeholders = challengeIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));
        String sql = "DELETE FROM tmp_finished_challenge WHERE challenge_id IN (" + placeholders + ")";
        jdbcTemplate.update(sql, challengeIds.toArray());
    }
}
