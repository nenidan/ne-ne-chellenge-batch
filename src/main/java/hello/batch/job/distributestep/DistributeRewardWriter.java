package hello.batch.job.distributestep;

import hello.batch.model.Reward;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 각 유저별로 실제 포인트를 증가시키고, 임시 테이블에서 해당 챌린지 정보를 삭제한다.
 */
public class DistributeRewardWriter implements ItemWriter<Reward> {

    private final JdbcTemplate jdbcTemplate;

    public DistributeRewardWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends Reward> chunk) throws Exception {
        long l = System.currentTimeMillis();

        chunk.getItems().forEach(r -> {
            distributeReward(r);
//            clearTempTable(r.getChallengeId());
        });

        List<Long> challengeIdsToDelete = chunk.getItems().stream()
            .map(Reward::getChallengeId)
            .toList();
//
//        jdbcTemplate.execute("CREATE TEMPORARY TABLE IF NOT EXISTS tmp_challenges (challenge_id BIGINT PRIMARY KEY)");
//        jdbcTemplate.update("TRUNCATE TABLE tmp_challenges");
//        String insertSql = "INSERT INTO tmp_challenges (challenge_id) VALUES (?)";
//        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                ps.setLong(1, challengeIdsToDelete.get(i));
//            }
//            @Override
//            public int getBatchSize() {
//                return challengeIdsToDelete.size();
//            }
//        });
//        long l3 = System.currentTimeMillis();
//        String deleteSql = "DELETE t FROM tmp_finished_challenge t JOIN tmp_challenges tmp ON t.challenge_id = tmp.challenge_id";
//        jdbcTemplate.update(deleteSql);
//        long l4 = System.currentTimeMillis();
//        System.out.println("Join deletion took: " + (l4 - l3) + "ms");
        clearTempTableWithIn(challengeIdsToDelete);

        long l2 = System.currentTimeMillis();
        System.out.println("Took: " + (l2 - l) + "ms to process one challenge");
    }

    private void distributeReward(Reward reward) {
        addPoints(reward.getUserRewards());
        writePointTransaction(reward);
    }

    private void writePointTransaction(Reward reward) {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        List<Map.Entry<Long, Integer>> entries = new ArrayList<>(reward.getUserRewards().entrySet());
        Map<Long, Long> userPointWallet = reward.getUserPointWallet();
        String sql = "INSERT INTO point_transaction(created_at, deleted_at, updated_at, amount, description, reason, point_wallet_id) " +
            "VALUES (?, null, ?, ?, '챌린지 보상 지급', 'CHALLENGE_REWARD', ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map.Entry<Long, Integer> entry = entries.get(i);
                Long userId = entry.getKey();
                Long pointWalletId = userPointWallet.get(userId);
                Integer amount = entry.getValue();

                ps.setTimestamp(1, nowTimestamp);
                ps.setTimestamp(2, nowTimestamp);
                ps.setInt(3, amount);
                ps.setLong(4, pointWalletId);
            }

            @Override
            public int getBatchSize() {
                return entries.size();
            }
        });
    }

    private void clearTempTable(Long challengeId) {
        jdbcTemplate.update("DELETE FROM tmp_finished_challenge WHERE challenge_id = ?", challengeId);
    }

    private void clearTempTableWithIn(List<Long> challengeIds) {
        String placeholders = challengeIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));
        String sql = "DELETE FROM tmp_finished_challenge WHERE challenge_id IN (" + placeholders + ")";
        jdbcTemplate.update(sql, challengeIds.toArray());
    }

    private void addPoints(Map<Long, Integer> userRewards) {
    String sql = "UPDATE point_wallet SET balance = balance + ? WHERE user_id = ?";

        List<Map.Entry<Long, Integer>> entries = new ArrayList<>(userRewards.entrySet());

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map.Entry<Long, Integer> entry = entries.get(i);
                ps.setInt(1, entry.getValue());
                ps.setLong(2, entry.getKey());
            }
            @Override
            public int getBatchSize() {
                return entries.size();
            }
        });
    }
}
