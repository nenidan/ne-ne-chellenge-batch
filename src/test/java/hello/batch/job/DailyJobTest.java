package hello.batch.job;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
class DailyJobTest {

    @Autowired
    JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("finishChallengeAndDistributeRewardJob")
    Job job;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        insertChallenges();
        insertPointWallet();
        insertHistory();
    }

    @AfterEach
    void clearTestData() {
        jdbcTemplate.update("DELETE FROM point_wallet WHERE user_id IN (99, 100, 101)");
        jdbcTemplate.update("DELETE FROM history WHERE user_id IN (99, 100, 101)");
        jdbcTemplate.update("DELETE FROM challenge WHERE id IN (99, 100, 101)");
    }

    @Test
    void executeDailyJob() throws Exception {
        // given
        JobParameters jobParameters = new JobParametersBuilder()
            .addJobParameter("targetDate", "2025-08-01", String.class)
            .toJobParameters();
        jobLauncherTestUtils.setJob(job);

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        String status = jdbcTemplate.queryForObject("SELECT status FROM challenge WHERE id = 99", String.class);
        assertThat(status).isEqualTo("FINISHED");

        Integer user99Point = jdbcTemplate.queryForObject("SELECT balance FROM point_wallet WHERE user_id = ?",
            Integer.class,
            99L
        );
        Integer user100Point = jdbcTemplate.queryForObject("SELECT balance FROM point_wallet WHERE user_id = ?",
            Integer.class,
            100L
        );
        Integer user101Point = jdbcTemplate.queryForObject("SELECT balance FROM point_wallet WHERE user_id = ?",
            Integer.class,
            101L
        );
        assertThat(user99Point).isEqualTo(1000);
        assertThat(user100Point).isEqualTo(0);
        assertThat(user101Point).isEqualTo(3000);

        Integer tempTableRow = jdbcTemplate.queryForObject("SELECT count(*) FROM tmp_finished_challenge",
            Integer.class
        );
        assertThat(tempTableRow).isEqualTo(0);
    }

    /**
     * 챌린지를 DB에 주입한다. 챌린지의 속성은 다음과 같다.
     * 99번 챌린지: 분배할 챌린지, 히스토리 주입 편의 상 기간 이틀로 가정
     * 100번 챌린지: 아직 안 끝난 챌린지
     * 101번 챌린지: 이미 끝난 챌린지, 중복 보상을 확인하는 목적
     */
    private void insertChallenges() {
        jdbcTemplate.update(
            "INSERT INTO challenge (id, status, participation_fee, total_fee, start_at, due_at, max_participants, min_participants, created_at) " +
                "VALUES (99, 'ONGOING', 1000, 1000, '2025-07-31', '2025-08-01', 2, 5, '2025-07-01')," +
                "(100, 'ONGOING', 2000, 2000, '2025-08-01', '2025-08-31', 2, 5, '2025-07-01')," +
                "(101, 'FINISHED', 3000, 3000, '2025-07-29', '2025-07-30', 2, 5, '2025-07-01')");
    }

    /**
     * 사용자의 포인트 지갑을 주입한다.
     * 99번 사용자: 99번 챌린지의 유일한 달성자, 잔액 0
     * 100번 사용자: 100번 챌린지 참가중
     * 101번 사용자: 101번 챌린지의 달성자, 포인트 이미 수령 -> 잔액 3000
     */
    private void insertPointWallet() {
        jdbcTemplate.update(
            "INSERT INTO point_wallet (balance, user_id, created_at) " +
                "VALUES (0, 99, '2025-01-01')," +
                "(0, 100, '2025-01-01')," +
                "(3000, 101, '2025-01-01')");
    }

    /**
     * 달성률 계산을 위해 히스토리를 주입한다.
     * 99번 사용자 - 99번 챌린지 2번 인증
     * 100번 사용자 - 100번 챌린지 1번 인증
     * 101번 사용자 - 101번 챌린지 2번 인증
     */
    private void insertHistory() {
        jdbcTemplate.update(
            "INSERT INTO history(user_id, challenge_id, is_success, created_at) VALUES " +
                "(99, 99, true, '2025-07-31'), (99, 99, true, '2025-08-01')," +
                "(100, 100, true, '2025-08-01')," +
                "(101, 101, true, '2025-07-29'), (101, 101, true, '2025-07-30')"
        );
    }

}
