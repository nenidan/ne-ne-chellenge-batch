package hello.batch.job.distributerewardstep;

import hello.batch.dto.RewardInfo;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DistributeRewardStepConfig {

    @Bean
    public Step distributeRewardStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        DataSource dataSource,
        JdbcTemplate jdbcTemplate
    ) {
        return new StepBuilder("DistributeRewardStep", jobRepository)
            .<RewardInfo, RewardInfo>chunk(1000, transactionManager)
            .reader(rewardInfoReader(dataSource))
            .processor(noOpRewardInfoProcessor())
            .writer(distributeRewardWriter(jdbcTemplate))
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
        factoryBean.setSortKey("user_id");

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
