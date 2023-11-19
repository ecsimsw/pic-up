package ecsimsw.picup.alert;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackMessageSender {

    private static final String webhookUrl = "https://hooks.slack.com/services/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";
    private static final Slack slack = Slack.getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackMessageSender.class);

    public static void send(String message) {
        try {
            var payload = Payload.builder().text(message).build();
            slack.send(webhookUrl, payload);
        }  catch (Exception e) {
            LOGGER.error("Failed to send slack alert");
        }
    }
}
