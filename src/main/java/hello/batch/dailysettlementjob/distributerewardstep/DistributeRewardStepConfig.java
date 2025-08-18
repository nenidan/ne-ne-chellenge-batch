package hello.batch.dailysettlementjob.distributerewardstep;

import hello.batch.dailysettlementjob.dto.RewardInfo;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DistributeRewardStepConfig {

    @Bean
    public Step distributeRewardStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JdbcTemplate jdbcTemplate,
        ItemReader<RewardInfo> rewardInfoReader
    ) {
        return new StepBuilder("DistributeRewardStep", jobRepository)
            .<RewardInfo, RewardInfo>chunk(1000, transactionManager)
            .reader(rewardInfoReader)
            .processor(noOpRewardInfoProcessor())
            .writer(distributeRewardWriter(jdbcTemplate))
            .faultTolerant()
            .retryLimit(3)
            .retry(CannotAcquireLockException.class) // 데드락 발생 시 청크별로 3번 재시도
            .build();
    }

    @Bean
    public ItemReader<RewardInfo> rewardInfoReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<RewardInfo>()
            .name("RewardInfoReader")
            .dataSource(dataSource)
            .pageSize(1000)
            .queryProvider(rewardInfoQueryProvider(dataSource))
            .rowMapper(resultInfoRowMapper())
            .build();
    }

    @Bean
    public PagingQueryProvider rewardInfoQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT challenge_id, user_id, amount");
        factoryBean.setFromClause("FROM tmp_reward_info");
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
