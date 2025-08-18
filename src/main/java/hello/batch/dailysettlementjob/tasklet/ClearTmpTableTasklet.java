package hello.batch.dailysettlementjob.tasklet;

import hello.batch.dailysettlementjob.slack.SlackService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClearTmpTableTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;
    private final SlackService slackService;

    public ClearTmpTableTasklet(JdbcTemplate jdbcTemplate, SlackService slackService) {
        this.jdbcTemplate = jdbcTemplate;
        this.slackService = slackService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Integer tmpFinishedChallengeRemainingCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM tmp_finished_challenge WHERE is_processed = 0", Integer.class);
        Integer tmpRewardInfoRemainingCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM tmp_reward_info WHERE is_processed = 0", Integer.class);

        if (tmpFinishedChallengeRemainingCount == 0) {
            jdbcTemplate.update("TRUNCATE TABLE tmp_finished_challenge");
        } else {
            jdbcTemplate.update("DELETE FROM tmp_finished_challenge WHERE is_processed = 1");
            slackService.sendMessage("There are " + tmpFinishedChallengeRemainingCount + " unprocessed challenges in tmp_finished_challenge");
        }

        if (tmpRewardInfoRemainingCount == 0) {
            jdbcTemplate.update("TRUNCATE TABLE tmp_reward_info");
        } else {
            jdbcTemplate.update("DELETE FROM tmp_reward_info WHERE is_processed = 1");
            slackService.sendMessage("There are " + tmpRewardInfoRemainingCount + " unprocessed reward in tmp_reward_info");
        }

        return RepeatStatus.FINISHED;
    }
}
