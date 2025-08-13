package hello.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DailySettlementJobConfig {

    @Bean
    public Job dailySettlementJob(JobRepository jobRepository,
        @Qualifier("finishChallengeStep") Step finishChallengeStep,
        @Qualifier("calculateRewardStep") Step calculateRewardStep,
        @Qualifier("distributeRewardStep") Step distributeRewardStep) {
        return new JobBuilder("DailySettlementJob", jobRepository)
            .start(finishChallengeStep)
            .next(calculateRewardStep)
            .next(distributeRewardStep)
            .build();
    }
}