package hello.batch.admin.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StatisticsJobConfig {

    private final StatisticsScheduler statisticsScheduler;

    public StatisticsJobConfig(StatisticsScheduler statisticsScheduler) {
        this.statisticsScheduler = statisticsScheduler;
    }

    @Bean
    public Job statisticsJob(JobRepository jobRepository, Step statisticsStep) {
        return new JobBuilder("statisticsJob", jobRepository)
                .start(statisticsStep)
                .build();
    }

    @Bean
    public Step statisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("statisticsStep", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Tasklet");
                        statisticsScheduler.runMonthlyStatisticsUpdate();
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }
}
