package hello.batch.launch;

import hello.batch.init.DataInitializer;
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
public class FinishStepPerformanceTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    DataInitializer dataInitializer;

    @Autowired
    @Qualifier("dailySettlementJob")
    Job job;

    @BeforeEach
    void before() {
//        dataInitializer.deleteAll();
        jobRepositoryTestUtils.removeJobExecutions();
    }

//    @Test
    void measureTime() throws InterruptedException {
        JobParameters jobParameters = new JobParametersBuilder()
            .addJobParameter("targetDate", TARGET_DATE.toString(), String.class)
            .toJobParameters();
        jobLauncherTestUtils.setJob(job);
        jobLauncherTestUtils.launchStep("finishChallengeStep", jobParameters);
    }
}
