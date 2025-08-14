package hello.batch.job;

import hello.batch.job.tasklet.ClearTmpTableTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DailySettlementJobConfig {

    @Bean
    public Job dailySettlementJob(JobRepository jobRepository,
        @Qualifier("finishChallengeStep") Step finishChallengeStep,
        @Qualifier("calculateRewardStep") Step calculateRewardStep,
        @Qualifier("distributeRewardStep") Step distributeRewardStep,
        @Qualifier("clearTmpTableStep") Step clearTmpTableStep
    ) {
        return new JobBuilder("DailySettlementJob", jobRepository)
            .start(finishChallengeStep)
            .next(calculateRewardStep)
            .next(distributeRewardStep)
            .next(clearTmpTableStep)
            .build();
    }

    @Bean
    public Step clearTmpTableStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
        ClearTmpTableTasklet clearTmpTableTasklet
    ) {
        return new StepBuilder("ClearTmpTableStep", jobRepository)
            .tasklet(clearTmpTableTasklet, transactionManager)
            .build();
    }
}