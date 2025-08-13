package hello.batch.job.calculaterewardstep;

import hello.batch.dto.ChallengeResult;
import hello.batch.dto.Reward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CalculateRewardProcessorTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ItemProcessor<ChallengeResult, Reward> processor;

    @BeforeEach
    void setUp() {
        // 챌린지 주입 (history FK)
        jdbcTemplate.update(
            "INSERT INTO challenge (id, status, participation_fee, total_fee, start_at, due_at, max_participants, min_participants, created_at, current_participant_count) " +
                "VALUES (99, 'ONGOING', 10000, 10000, '2025-07-01', '2025-08-01', 2, 5, '2025-07-01', 1)");

        // 99번 사용자 이틀치 인증 기록 주입
        jdbcTemplate.update(
            "INSERT INTO history(id, user_id, challenge_id, is_success, created_at) VALUES (99, 99, 99, true, '2025-07-01'), " +
                "(100, 99, 99, true, '2025-07-02')"
        );
    }

    @Test
    void process() throws Exception {
        // given
        ChallengeResult challengeResult = new ChallengeResult(99L, 10000, 2);

        // when
        Reward reward = processor.process(challengeResult);

        // then
        Map<Long, Integer> userRewards = reward.getUserRewards();
        assertThat(userRewards).hasSize(1);
        assertThat(userRewards.get(99L)).isGreaterThanOrEqualTo(10000);
    }
}