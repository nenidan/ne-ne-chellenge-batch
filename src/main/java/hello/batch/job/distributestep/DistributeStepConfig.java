package hello.batch.job.distributestep;

import hello.batch.model.ChallengeResult;
import hello.batch.model.Reward;
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
public class DistributeStepConfig {

    @Bean
    public Step distributeRewardStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<ChallengeResult> challengeResultReader,
        ItemProcessor<ChallengeResult, Reward> calculateRewardProcessor,
        ItemWriter<Reward> distributeRewardWriter
    ) {
        return new StepBuilder("distributeChanllengeStep", jobRepository)
            .<ChallengeResult, Reward>chunk(10, transactionManager)
            .reader(challengeResultReader)
            .processor(calculateRewardProcessor)
            .writer(distributeRewardWriter)
            .build();
    }

    @Bean
    public ItemReader<ChallengeResult> challengeResultReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<ChallengeResult>()
            .name("challengeResultReader")
            .dataSource(dataSource)
            .pageSize(100)
            .queryProvider(challengeResultQueryProvider(dataSource))
            .rowMapper(challengeResultRowMapper())
            .build();
    }

    @Bean
    public PagingQueryProvider challengeResultQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT id, challenge_id, total_fee, total_days");
        factoryBean.setFromClause("FROM tmp_finished_challenge");
        factoryBean.setSortKey("id");

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
    public ItemWriter<Reward> distributeRewardWriter(JdbcTemplate jdbcTemplate) {
        return new DistributeRewardWriter(jdbcTemplate);
    }
}
