package hello.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

//    @Bean
//    public Job finishChallengeAndDistributeRewardJob(JobRepository jobRepository) {
//        return new JobBuilder("finishChallengeAndDistributeRewardJob", jobRepository)
//            .start()
//            .build();
//    }
}