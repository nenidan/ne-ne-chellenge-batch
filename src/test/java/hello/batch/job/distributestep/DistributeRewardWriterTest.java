package hello.batch.job.distributestep;

import hello.batch.model.Reward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DistributeRewardWriterTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ItemWriter<Reward> writer;

    @BeforeEach
    void setUp() {
        // 99번 챌린지 주입 (history FK)
        jdbcTemplate.update(
            "INSERT INTO challenge (id, status, participation_fee, total_fee, start_at, due_at, max_participants, min_participants, created_at, current_participant_count) " +
                "VALUES (99, 'ONGOING', 20000, 20000, '2025-07-01', '2025-08-01', 2, 5, '2025-07-01', 1)");

        // 99번 유저 포인트 지갑 주입
        jdbcTemplate.update(
            "INSERT INTO point_wallet (id, balance, user_id, created_at) " +
                "VALUES (99, 0, 99, '2025-08-01')");

        // 임시 테이블 row 생성
        jdbcTemplate.update(
            "INSERT INTO tmp_finished_challenge (id, challenge_id, total_fee, total_days) " +
                "VALUES (99, 99, 20000, 2)");
    }

    @Test
    void write() throws Exception {
        // given
        Map<Long, Integer> userReward = Map.of(99L, 20000);
        Map<Long, Long> userPointWallet = Map.of(99L, 99L);
        List<Reward> rewards = List.of(new Reward(99L, userReward, userPointWallet));
        Chunk<Reward> chunk = new Chunk<>(rewards);

        // when
        writer.write(chunk);

        // then
        Integer point = jdbcTemplate.queryForObject("SELECT balance FROM point_wallet WHERE user_id = ?",
            Integer.class,
            99L
        );
        assertThat(point).isEqualTo(20000);

        Integer tempTableRow = jdbcTemplate.queryForObject("select count(*) from tmp_finished_challenge",
            Integer.class
        );
        assertThat(tempTableRow).isEqualTo(0);

        Integer transactionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM point_transaction WHERE point_wallet_id = ?",
            Integer.class,
            99
        );
        assertThat(transactionCount).isEqualTo(1);
    }
}