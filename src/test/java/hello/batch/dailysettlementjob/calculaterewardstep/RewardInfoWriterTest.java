package hello.batch.dailysettlementjob.calculaterewardstep;

import hello.batch.dailysettlementjob.dto.Reward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RewardInfoWriterTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ItemWriter<Reward> writer;

    @BeforeEach
    void setUp() {
        // 임시 테이블 row 생성
        jdbcTemplate.update(
            "INSERT INTO tmp_finished_challenge (challenge_id, total_fee, total_days) " +
                "VALUES (99, 20000, 2)");
    }

    @Test
    void write() throws Exception {
        // given
        Map<Long, Integer> userReward = Map.of(99L, 20000);
        List<Reward> rewards = List.of(new Reward(99L, userReward));
        Chunk<Reward> chunk = new Chunk<>(rewards);

        // when
        writer.write(chunk);

        // then
        Integer amount = jdbcTemplate.queryForObject("SELECT amount FROM tmp_reward_info WHERE user_id = 99 AND challenge_id = 99", Integer.class);
        assertThat(amount).isEqualTo(20000);

        Integer tempTableRow = jdbcTemplate.queryForObject("select count(*) from tmp_finished_challenge WHERE is_processed = 0",
            Integer.class
        );
        assertThat(tempTableRow).isEqualTo(0);
    }
}