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
        chunk.getItems().forEach(r -> {
            distributeReward(r);
            clearTempTable(r.getChallengeId());
        });
    }

    private void distributeReward(Reward reward) {
        reward.getUserRewards().forEach(this::addPoint);
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

    private void addPoint(Long userId, int point) {
        String sql = "UPDATE point_wallet SET balance = balance + ? WHERE user_id = ?";
        jdbcTemplate.update(sql, point, userId);
    }
}
