package hello.batch.job.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClearTmpTableTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(ClearTmpTableTasklet.class);

    private final JdbcTemplate jdbcTemplate;

    public ClearTmpTableTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Integer tmpFinishedChallengeRemainingCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM tmp_finished_challenge WHERE is_processed = 0", Integer.class);
        Integer tmpRewardInfoRemainingCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM tmp_reward_info WHERE is_processed = 0", Integer.class);

        if (tmpFinishedChallengeRemainingCount == 0) {
            jdbcTemplate.update("TRUNCATE TABLE tmp_finished_challenge");
        } else {
            jdbcTemplate.update("DELETE FROM tmp_finished_challenge WHERE is_processed = 1");
            log.error("There are {} unprocessed challenges in tmp_finished_challenge", tmpFinishedChallengeRemainingCount);
        }

        if (tmpRewardInfoRemainingCount == 0) {
            jdbcTemplate.update("TRUNCATE TABLE tmp_reward_info");
        } else {
            jdbcTemplate.update("DELETE FROM tmp_reward_info WHERE is_processed = 1");
            log.error("There are {} unprocessed reward in tmp_reward_info", tmpRewardInfoRemainingCount);
        }

        return RepeatStatus.FINISHED;
    }
}
