package hello.batch.dailysettlementjob.distributerewardstep;

import hello.batch.dailysettlementjob.dto.RewardInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@Transactional
@ActiveProfiles("test")
class RewardInfoReaderTest {

    @Autowired
    ItemReader<RewardInfo> rewardInfoReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO tmp_reward_info(user_id, challenge_id, amount) VALUES" +
            "(99, 99, 1000)");
    }

    @Test
    void read() throws Exception {
        // given

        // when
        RewardInfo result = rewardInfoReader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(99);
        assertThat(result.getChallengeId()).isEqualTo(99);
        assertThat(result.getAmount()).isEqualTo(1000);
    }
}