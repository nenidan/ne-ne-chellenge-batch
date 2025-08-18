package hello.batch.dailysettlementjob.listener;

import hello.batch.dailysettlementjob.slack.SlackService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FailedJobListener implements JobExecutionListener {

    private final SlackService slackService;

    public FailedJobListener(SlackService slackService) {
        this.slackService = slackService;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            StringBuilder sb = new StringBuilder();
            sb.append("Job " + jobExecution.getJobInstance().getJobName() + " failed \n");
            sb.append("Target Date: " + jobExecution.getJobParameters().getString("targetDate") + "\n");
            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                if (stepExecution.getStatus() == BatchStatus.FAILED) {
                    sb.append("Failed Step: " + stepExecution.getStepName() + "\n");
                    sb.append("Exceptions: " + jobExecution.getAllFailureExceptions());
                }
            }

            slackService.sendMessage(sb.toString());
        }
    }
}
