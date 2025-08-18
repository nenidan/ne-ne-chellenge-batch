package hello.batch.dailysettlementjob.calculaterewardstep;

import hello.batch.dailysettlementjob.dto.ChallengeResult;
import hello.batch.dailysettlementjob.dto.Reward;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class CalculateRewardStepConfig {

    @Bean
    public Step calculateRewardStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<ChallengeResult> challengeResultReader,
        ItemProcessor<ChallengeResult, Reward> calculateRewardProcessor,
        ItemWriter<Reward> rewardInfoWriter
    ) {
        return new StepBuilder("CalculateRewardStep", jobRepository)
            .<ChallengeResult, Reward>chunk(1000, transactionManager)
            .reader(challengeResultReader)
            .processor(calculateRewardProcessor)
            .writer(rewardInfoWriter)
            .taskExecutor(threadPoolTaskExecutor())
            .build();
    }

    private TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(20);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    public ItemReader<ChallengeResult> challengeResultReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<ChallengeResult>()
            .name("ChallengeResultReader")
            .dataSource(dataSource)
            .pageSize(1000)
            .queryProvider(challengeResultQueryProvider(dataSource))
            .rowMapper(challengeResultRowMapper())
            .build();
    }

    @Bean
    public PagingQueryProvider challengeResultQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT challenge_id, total_fee, total_days");
        factoryBean.setFromClause("FROM tmp_finished_challenge");
        factoryBean.setSortKey("challenge_id");

        try {
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get challenge result", e);
        }
    }

    private RowMapper<ChallengeResult> challengeResultRowMapper() {
        return (rs, rowNum) -> new ChallengeResult(
            rs.getLong("challenge_id"),
            rs.getInt("total_fee"),
            rs.getInt("total_days")
        );
    }

    @Bean
    public ItemProcessor<ChallengeResult, Reward> calculateRewardProcessor(JdbcTemplate jdbcTemplate) {
        return new CalculateRewardProcessor(jdbcTemplate);
    }

    @Bean
    public ItemWriter<Reward> rewardInfoWriter(JdbcTemplate jdbcTemplate) {
        return new RewardInfoWriter(jdbcTemplate);
    }
}
