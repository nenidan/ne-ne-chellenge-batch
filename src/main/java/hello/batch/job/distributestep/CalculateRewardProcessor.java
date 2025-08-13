package hello.batch.job.distributestep;

import hello.batch.model.ChallengeResult;
import hello.batch.model.Reward;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 해당 챌린지의 달성자와 보상 포인트를 계산하여 결과를 Reward 형태로 Writer에 전달한다.
 */
public class CalculateRewardProcessor implements ItemProcessor<ChallengeResult, Reward> {

    private final JdbcTemplate jdbcTemplate;

    public CalculateRewardProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reward process(ChallengeResult result) throws Exception {
        Map<Long, Integer> userRewardMap = new HashMap<>();

        Long challengeId = result.getChallengeId();
        List<Long> winners = getWinners(challengeId, result.getTotalDays());

        if (!winners.isEmpty()) {
            int reward = result.getTotalFee() / winners.size();
            for (Long winner : winners) {
                userRewardMap.put(winner, reward);
            }
        }

        Map<Long, Long> userPointWallet = getUserPointWallet(winners);

        return new Reward(challengeId, userRewardMap, userPointWallet);
    }

    private Map<Long, Long> getUserPointWallet(List<Long> winners) {
        if (winners.isEmpty()) return Map.of();

        String inClause = winners.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", ", "(", ")"));

        String sql = """
        SELECT user_id, id
        FROM point_wallet
        WHERE user_id IN %s AND deleted_at IS NULL
        """.formatted(inClause);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, winners.toArray());

        Map<Long, Long> userPointWallet = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long userId = ((Number) row.get("user_id")).longValue();
            Long walletId = ((Number) row.get("id")).longValue();
            userPointWallet.put(userId, walletId);
        }

        return userPointWallet;
    }

    private List<Long> getWinners(Long challengeId, Integer totalDays) {
        Map<Long, Integer> userHistory = getUserHistory(challengeId);

        List<Long> winners = userHistory.entrySet().stream()
            .filter(new WinnerFilter(totalDays))
            .map(Map.Entry::getKey)
            .toList();

        return winners;
    }

    private static class WinnerFilter implements Predicate<Map.Entry<Long, Integer>> {

        private static final int TARGET_SUCCESS_RATE = 70;

        private final int totalDays;

        public WinnerFilter(int totalDays) {
            this.totalDays = totalDays;
        }

        @Override
        public boolean test(Map.Entry<Long, Integer> longIntegerEntry) {
            int historyCount = longIntegerEntry.getValue();

            return (100 * historyCount / totalDays) >= TARGET_SUCCESS_RATE;
        }
    }

    private Map<Long, Integer> getUserHistory(Long challengeId) {
        String sql = """
            SELECT user_id, COUNT(*) AS count
            FROM history
            WHERE challenge_id = ? AND is_success = true
            GROUP BY user_id
            """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, challengeId);
        Map<Long, Integer> collect = rows.stream()
            .collect(Collectors.toMap(
                row -> ((Number) row.get("user_id")).longValue(),
                row -> ((Number) row.get("count")).intValue()
            ));

        return collect;
    }
}
