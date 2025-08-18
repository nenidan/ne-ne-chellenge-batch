package hello.batch.dailysettlementjob.distributerewardstep;

import hello.batch.dailysettlementjob.dto.RewardInfo;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DistributeRewardWriter implements ItemWriter<RewardInfo> {

    private final JdbcTemplate jdbcTemplate;

    public DistributeRewardWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * (user_id, challenge_id, amount)를 읽어와
     * point_transaction 기록 생성, point_wallet에 amount를 증가시킨다.
     * tmp_reward_info의 is_processed를 true로 설정한다.
     * @param chunk of items to be written. Must not be {@code null}.
     * @throws Exception
     */
    @Override
    public void write(Chunk<? extends RewardInfo> chunk) {
        List<? extends RewardInfo> rewardInfos = chunk.getItems();

        updateTmpRewardInfo(rewardInfos);

        List<Long> userIdList = rewardInfos.stream().map(RewardInfo::getUserId).toList();
        Map<Long, Long> userPointWalletMap = getUserPointWallet(userIdList); // (user_id, point_wallet_id)

        writePointTransaction(rewardInfos, userPointWalletMap);

        addPoint(rewardInfos, userPointWalletMap);
    }

    private void writePointTransaction(List<? extends RewardInfo> rewardInfoList, Map<Long, Long> userPointWalletMap) {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        String sql = "INSERT INTO point_transaction(created_at, deleted_at, updated_at, amount, description, reason, point_wallet_id) " +
            "VALUES (?, null, ?, ?, '챌린지 보상 지급', 'CHALLENGE_REWARD', ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    RewardInfo rewardInfo = rewardInfoList.get(i);
                    Long pointWalletId = userPointWalletMap.get(rewardInfo.getUserId());

                    ps.setTimestamp(1, nowTimestamp);
                    ps.setTimestamp(2, nowTimestamp);
                    ps.setInt(3, rewardInfo.getAmount());
                    ps.setLong(4, pointWalletId);
                }

                @Override
                public int getBatchSize() {
                    return rewardInfoList.size();
                }
            }
        );
    }

    private void updateTmpRewardInfo(List<? extends RewardInfo> rewardInfos) {
        String placeholders = rewardInfos.stream()
            .map(r -> "(?, ?)")
            .collect(Collectors.joining(", "));

        String sql = "UPDATE tmp_reward_info SET is_processed = 1 WHERE (user_id, challenge_id) IN (" + placeholders + ")";

        // 파라미터 배열을 (userId1, challengeId1, userId2, challengeId2, ...) 형태로 만들기
        Object[] params = rewardInfos.stream()
            .flatMap(r -> Stream.of(r.getUserId(), r.getChallengeId()))
            .toArray();

        jdbcTemplate.update(sql, params);
    }

    private void addPoint(List<? extends RewardInfo> rewardInfos, Map<Long, Long> userPointWalletMap) {
        Map<Long, Integer> pointWalletRewards = new HashMap<>();
        for (RewardInfo r : rewardInfos) {
            Long pointWalletId = userPointWalletMap.get(r.getUserId());
            pointWalletRewards.merge(pointWalletId, r.getAmount(), Integer::sum);
        }

        List<Map.Entry<Long, Integer>> entries = pointWalletRewards.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .toList();

        String sql = "UPDATE point_wallet SET balance = balance + ? WHERE id = ?";

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

    private Map<Long, Long> getUserPointWallet(List<Long> users) {
        if (users.isEmpty())
            return Map.of();

        String inClause = users.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", ", "(", ")"));

        String sql = """
            SELECT user_id, id
            FROM point_wallet
            WHERE user_id IN %s AND deleted_at IS NULL
            """.formatted(inClause);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, users.toArray());

        Map<Long, Long> userPointWallet = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long userId = ((Number) row.get("user_id")).longValue();
            Long walletId = ((Number) row.get("id")).longValue();
            userPointWallet.put(userId, walletId);
        }

        return userPointWallet;
    }
}
