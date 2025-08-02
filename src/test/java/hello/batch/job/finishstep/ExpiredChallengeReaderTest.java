package hello.batch.job.finishstep;

import hello.batch.model.Challenge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@Transactional
public class ExpiredChallengeReaderTest {

    @Autowired
    ItemReader<Challenge> challengeReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final static String TARGET_DATE_STR = "2025-08-01";

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

    public StepExecution getStepExection() {
        JobParameters params = new JobParametersBuilder()
            .addString("targetDate", TARGET_DATE_STR)
            .toJobParameters();

        return MetaDataInstanceFactory.createStepExecution(params);
    }

    @Test
    void read() throws Exception {
        // given
        StepExecution stepExecution = getStepExection();

        // when
        List<Challenge> results = StepScopeTestUtils.doInStepScope(stepExecution, () -> {
                List<Challenge> list = new ArrayList<>();
                Challenge challenge;

                // open/close 없이 바로 read() 호출
                while ((challenge = challengeReader.read()) != null) {
                    list.add(challenge);
                }

                return list;
            }
        );

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("id").containsExactlyInAnyOrder(99L, 100L);
        assertThat(results).allMatch(challenge -> challenge.getDueAt().isEqual(LocalDate.of(2025, 8, 1)));
        assertThat(results).anyMatch(challenge -> challenge.getId() == 99 && challenge.getTotalFee() == 1000);
        assertThat(results).anyMatch(challenge -> challenge.getId() == 100 && challenge.getTotalFee() == 2000);
    }
}