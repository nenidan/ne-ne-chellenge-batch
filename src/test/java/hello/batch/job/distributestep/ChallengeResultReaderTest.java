package hello.batch.job.distributestep;

import hello.batch.model.ChallengeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ChallengeResultReaderTest {

    @Autowired
    ItemReader<ChallengeResult> challengeResultReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
            "INSERT INTO tmp_finished_challenge(id, challenge_id, total_fee, total_days) VALUES (1, 1, 1000, 1)");
        jdbcTemplate.update(
            "INSERT INTO tmp_finished_challenge(id, challenge_id, total_fee, total_days) VALUES (2, 1, 2000, 2)");
    }

    @Test
    void read() throws Exception {
        // given

        // when
        List<ChallengeResult> results = new ArrayList<>();
        ChallengeResult challengeResult;
        while ((challengeResult = challengeResultReader.read()) != null) {
            results.add(challengeResult);
        }

        // then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getChallengeId() == 1);
    }
}
