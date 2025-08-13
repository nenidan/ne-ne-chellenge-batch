package hello.batch.launch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static hello.batch.init.DataInitializer.TARGET_DATE;

@SpringBootTest
@SpringBatchTest
public class DailySettlementJobPerformanceTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    JobRepositoryTestUtils jobRepositoryTestUtils;

//    @BeforeEach
    void before() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Autowired
    @Qualifier("dailySettlementJob")
    Job job;

    @Test
    void measureTime() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addJobParameter("targetDate", TARGET_DATE.toString(), String.class)
            .toJobParameters();

        jobLauncherTestUtils.setJob(job);
        long start = System.currentTimeMillis();
        jobLauncherTestUtils.launchJob(jobParameters);
        long end = System.currentTimeMillis();
        System.out.println(end - start + " ms");
    }
}
