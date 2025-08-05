package hello.batch.job.finishstep;

import hello.batch.model.ChallengeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ExpiredChallengeWriterTest {

    @Autowired
    ItemWriter<ChallengeResult> expiredChallengeWriter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
            "INSERT INTO challenge (id, status, participation_fee, total_fee, start_at, due_at, max_participants, min_participants, created_at) " +
                "VALUES (99, 'ONGOING', 1000, 1000, '2025-07-01', '2025-08-01', 2, 5, '2025-07-01')");
        jdbcTemplate.update(
            "INSERT INTO challenge (id, status, participation_fee, total_fee, start_at, due_at, max_participants, min_participants, created_at) " +
                "VALUES (100, 'ONGOING', 2000, 2000, '2025-07-01', '2025-08-01', 2, 5, '2025-07-01')");
        jdbcTemplate.update(
            "INSERT INTO challenge (id, status, participation_fee, total_fee, start_at, due_at, max_participants, min_participants, created_at) " +
                "VALUES (101, 'ONGOING', 3000, 3000, '2025-07-01', '2025-07-30', 2, 5, '2025-07-01')");
        jdbcTemplate.update(
            "INSERT INTO challenge (id, status, participation_fee, total_fee, start_at, due_at, max_participants, min_participants, created_at) " +
                "VALUES (102, 'FINISHED',4000, 4000, '2025-07-01', '2025-08-01', 2, 5, '2025-07-01')");
    }

    /*
     * challenge 테이블에서 상태가 ONGOING이었던 챌린지의 상태를 FINISHED로 변경하고
     * tmp_finished_challenge 테이블에 challenge_id, total_fee, total_days를 저장한다.
     */
    @Test
    void write() throws Exception {
        // given
        int days = (int) ChronoUnit.DAYS.between(LocalDate.of(2025,7,1), LocalDate.of(2025,8,1)) + 1;
        ChallengeResult result1 = new ChallengeResult(99L, 1000, days);
        ChallengeResult result2 = new ChallengeResult(100L, 2000, days);
        Chunk<? extends ChallengeResult> chunk = Chunk.of(result1, result2);

        // when
        expiredChallengeWriter.write(chunk);

        // then
        Integer updatedStatusCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM challenge WHERE id IN (99, 100) AND status = 'FINISHED'",
            Integer.class
        );
        assertThat(updatedStatusCount).isEqualTo(2);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM tmp_finished_challenge WHERE challenge_id IN(99, 100)"
        );
        assertThat(rows).hasSize(2);

        Map<String, Object> row1 = rows.get(0);
        assertThat(row1.get("challenge_id")).isEqualTo(99L);
        assertThat(row1.get("total_fee")).isEqualTo(1000);
        assertThat(row1.get("total_days")).isEqualTo(days);

        Map<String, Object> row2 = rows.get(1);
        assertThat(row2.get("challenge_id")).isEqualTo(100L);
        assertThat(row2.get("total_fee")).isEqualTo(2000);
        assertThat(row2.get("total_days")).isEqualTo(days);
    }
}