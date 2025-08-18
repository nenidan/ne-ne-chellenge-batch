package hello.batch.dailysettlementjob.slack;

import com.slack.api.Slack;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.slack.api.webhook.WebhookPayloads.payload;

@Component
public class SlackService {
    @Value("${webhook.slack.url}")
    private String SLACK_WEBHOOK_URL;

    private final Slack slack = Slack.getInstance();

    public void sendMessage(String message) {
        try {
            slack.send(SLACK_WEBHOOK_URL, payload(p -> p.text(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
