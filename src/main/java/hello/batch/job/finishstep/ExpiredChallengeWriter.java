package hello.batch.job.finishstep;

import hello.batch.model.ChallengeResult;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 상태가 ONGOING이었던 챌린지의 상태를 FINISHED로 변경하고
 * tmp_finished_challenge 테이블에 challenge_id, total_fee, total_days를 저장한다.
 */
public class ExpiredChallengeWriter implements ItemWriter<ChallengeResult> {

    private final JdbcTemplate jdbcTemplate;

    public ExpiredChallengeWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends ChallengeResult> chunk) throws Exception {
        List<? extends ChallengeResult> challengeResultList = chunk.getItems();
        finishChallenges(challengeResultList);
        writeChallengeResult(challengeResultList);
    }

    private void finishChallenges(List<? extends ChallengeResult> challengeResultList) {
        String sql = "UPDATE challenge SET status = 'FINISHED' WHERE id = ?";

        List<Object[]> batchArgs = challengeResultList.stream()
            .map(cr -> new Object[]{cr.getChallengeId()})
            .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void writeChallengeResult(List<? extends ChallengeResult> list) {
        String sql = "INSERT INTO tmp_finished_challenge(challenge_id, total_fee, total_days) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql,
            new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, list.get(i).getChallengeId());
                    ps.setLong(2, list.get(i).getTotalFee());
                    ps.setLong(3, list.get(i).getTotalDays());
                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            }
        );
    }
}
