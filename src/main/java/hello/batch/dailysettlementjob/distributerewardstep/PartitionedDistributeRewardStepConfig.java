package hello.batch.dailysettlementjob.distributerewardstep;

import hello.batch.dailysettlementjob.dto.RewardInfo;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// 현재 싱글스레드와 파티션된 Step의 성능 차이가 없으므로 참고용으로 남겨만 둔다.
// @Configuration
public class PartitionedDistributeRewardStepConfig {
    // Manager Step
    @Bean
    public Step distributeRewardStep(JobRepository jobRepository,
        PointWalletPartitioner partitioner,
        Step distributeRewardWorkerStep
    ) {
        return new StepBuilder("DistributeRewardStep", jobRepository)
            .partitioner("DistributeRewardWorkerStep", partitioner)
            .step(distributeRewardWorkerStep)
            .gridSize(4)
            .taskExecutor(threadPoolTaskExecutor())
            .build();
    }

    // Worker Step
    @Bean
    public Step distributeRewardWorkerStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JdbcTemplate jdbcTemplate,
        ItemReader<RewardInfo> rewardInfoReader
    ) {
        return new StepBuilder("DistributeRewardWorkerStep", jobRepository)
            .<RewardInfo, RewardInfo>chunk(1000, transactionManager)
            .reader(rewardInfoReader)
            .processor(noOpRewardInfoProcessor())
            .writer(distributeRewardWriter(jdbcTemplate))
            .build();
    }

    private TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public ItemReader<RewardInfo> rewardInfoReader(DataSource dataSource,
        @Value("#{stepExecutionContext['minUserId']}") Long minUserId,
        @Value("#{stepExecutionContext['maxUserId']}") Long maxUserId) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("minUserId", minUserId);
        parameterValues.put("maxUserId", maxUserId);

        return new JdbcPagingItemReaderBuilder<RewardInfo>()
            .name("RewardInfoReader")
            .dataSource(dataSource)
            .pageSize(1000)
            .queryProvider(rewardInfoQueryProvider(dataSource))
            .parameterValues(parameterValues)
            .rowMapper(resultInfoRowMapper())
            .build();
    }

    @Bean
    public PagingQueryProvider rewardInfoQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT challenge_id, user_id, amount");
        factoryBean.setFromClause("FROM tmp_reward_info");
        factoryBean.setWhereClause("user_id BETWEEN :minUserId AND :maxUserId");
        factoryBean.setSortKeys(Map.of(
            "challenge_id", Order.ASCENDING,
            "user_id", Order.ASCENDING
        ));

        try {
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get reward info", e);
        }
    }

    private RowMapper<RewardInfo> resultInfoRowMapper() {
        return (rs, rowNum) -> new RewardInfo(
            rs.getLong("challenge_id"),
            rs.getLong("user_id"),
            rs.getInt("amount")
        );
    }

    @Bean
    public ItemProcessor<RewardInfo, RewardInfo> noOpRewardInfoProcessor() {
        return rewardInfo -> rewardInfo;
    }

    @Bean
    public ItemWriter<RewardInfo> distributeRewardWriter(JdbcTemplate jdbcTemplate) {
        return new DistributeRewardWriter(jdbcTemplate);
    }
}
