package hello.batch.init.job;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class GenerateHistoryProcessor implements ItemProcessor<ChallengeToGenerateHistory, List<History>> {

    @Override
    public List<History> process(ChallengeToGenerateHistory challenge) throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long totalDays = ChronoUnit.DAYS.between(challenge.getStartAt(), challenge.getDueAt()) + 1;
        int participants = challenge.getCurrentParticipantCount();
        Set<Long> userIds = new HashSet<>();
        while (userIds.size() < participants) {
            userIds.add(ThreadLocalRandom.current().nextLong(1, 1_000_001));
        }

        List<History> allHistories = new ArrayList<>();
        for (Long userId : userIds) {
            boolean isAchiever = random.nextDouble() <= 0.1;
            int historyCount = isAchiever ? (int) totalDays : (int) (totalDays * 0.1);

            for (int i = 0; i < historyCount; i++) {
                LocalDate date = challenge.getStartAt().plusDays(i);
                History history = new History(userId,
                    challenge.getId(),
                    true,
                    "mock content",
                    date,
                    date.atStartOfDay()
                );
                allHistories.add(history);
            }
        }

        return allHistories;
    }
}
