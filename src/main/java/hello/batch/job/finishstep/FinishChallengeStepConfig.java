package hello.batch.job.finishstep;

import hello.batch.model.Challenge;
import hello.batch.model.ChallengeResult;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class FinishChallengeStepConfig {

    @Bean
    public Step finishChallengeStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<Challenge> expiredChallengeReader,
        ItemProcessor<Challenge, ChallengeResult> finishedChallengeProcessor,
        ItemWriter<ChallengeResult> finishedChallengeWriter
    ) {
        return new StepBuilder("finishChallengeStep", jobRepository)
            .<Challenge, ChallengeResult>chunk(100, transactionManager)
            .reader(expiredChallengeReader)
            .processor(finishedChallengeProcessor)
            .writer(finishedChallengeWriter)
            .build();
    }

    /**
     * @param targetDateString JobParameter로 등록된 처리하고자 하는 날짜
     *                         FinishedChallengeReader는 due_at이 targetDate이고, status가 ONGOING인 row를 읽는다.
     */
    @StepScope
    @Bean
    public ItemReader<Challenge> finishedChallengeReader(DataSource dataSource,
        @Value("#{jobParameters['targetDate']}") String targetDateString
    ) {
        return new JdbcPagingItemReaderBuilder<Challenge>()
            .name("finishedChallengeReader")
            .dataSource(dataSource)
            .pageSize(100)
            .queryProvider(expiredChallengeQueryProvider(dataSource))
            .parameterValues(Map.of("targetDate", targetDateString))
            .rowMapper(challengeRowMapper())
            .build();
    }

    @Bean
    public PagingQueryProvider expiredChallengeQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT id, status, total_fee, start_at, due_at");
        factoryBean.setFromClause("FROM challenge");
        factoryBean.setWhereClause("WHERE status = 'ONGOING' AND due_at = :targetDate");
        factoryBean.setSortKey("id");

        try {
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get expired challenge data", e);
        }
    }

    private RowMapper<Challenge> challengeRowMapper() {
        return (rs, rowNum) -> new Challenge(
            rs.getLong("id"),
            rs.getDate("start_at").toLocalDate(),
            rs.getDate("due_at").toLocalDate(),
            rs.getInt("total_fee")
        );
    }

    @Bean
    public ItemProcessor<Challenge, ChallengeResult> finishedChallengeProcessor(JdbcTemplate jdbcTemplate) {
        return new ExpiredChallengeProcessor();
    }

    @Bean
    ItemWriter<ChallengeResult> expiredChallengeWriter(JdbcTemplate jdbcTemplate) {
        return new ExpiredChallengeWriter(jdbcTemplate);
    }
}
