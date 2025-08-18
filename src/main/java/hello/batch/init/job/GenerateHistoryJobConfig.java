package hello.batch.init.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 오늘 처리할 챌린지만 담긴 테이블을 읽어와서 히스토리를 생성한다.
 */
//@Configuration
public class GenerateHistoryJobConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean(name = "generateHistoryJob")
    public Job generateHistoryJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
        @Qualifier("generateHistoryStep") Step generateHistoryStep) {
        return new JobBuilder("generateHistoryJob", jobRepository)
            .start(generateHistoryStep).build();
    }

    @Bean
    public Step generateHistoryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
        DataSource dataSource, GenerateHistoryProcessor processor
    ) {
        return new StepBuilder("generateHistoryStep", jobRepository)
            .<ChallengeToGenerateHistory, List<History>>chunk(1000, transactionManager)
            .reader(allChallengeReader(dataSource))
            .processor(processor)
            .writer(writer())
            .taskExecutor(taskExecutor())
            .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }

    public GenerateHistoryJobConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public ItemWriter<List<History>> writer() {
        return items -> {
            List<History> flatList = items.getItems().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(
                "INSERT INTO history (user_id, challenge_id, date, is_success, created_at) VALUES (?, ?, ?, ?, ?)",
                flatList,
                1000, // 배치 크기
                (ps, history) -> {
                    ps.setLong(1, history.getUserId());
                    ps.setLong(2, history.getChallengeId());
                    ps.setObject(3, history.getDate());
                    ps.setBoolean(4, history.getIsSuccess());
                    ps.setTimestamp(5, Timestamp.valueOf(history.getCreatedAt()));
                }
            );
        };
    }

    @StepScope
    @Bean
    public ItemReader<ChallengeToGenerateHistory> allChallengeReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<ChallengeToGenerateHistory>()
            .name("allChallengeReader")
            .dataSource(dataSource)
            .pageSize(10000)
            .queryProvider(allChallengeQueryProvider(dataSource))
            .rowMapper(challengeToGenerateHistoryRowMapper())
            .build();
    }

    @Bean
    public PagingQueryProvider allChallengeQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT id, total_fee, start_at, due_at, current_participant_count");
        factoryBean.setFromClause("FROM challenge");
        factoryBean.setSortKey("id");

        try {
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get challenge data", e);
        }
    }

    private RowMapper<ChallengeToGenerateHistory> challengeToGenerateHistoryRowMapper() {
        return (rs, rowNum) -> new ChallengeToGenerateHistory(
            rs.getLong("id"),
            rs.getDate("start_at").toLocalDate(),
            rs.getDate("due_at").toLocalDate(),
            rs.getInt("total_fee"),
            rs.getInt("current_participant_count")
        );
    }
}
