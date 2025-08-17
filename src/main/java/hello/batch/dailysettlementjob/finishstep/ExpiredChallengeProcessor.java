package hello.batch.dailysettlementjob.finishstep;

import hello.batch.dailysettlementjob.dto.Challenge;
import hello.batch.dailysettlementjob.dto.ChallengeResult;
import org.springframework.batch.item.ItemProcessor;

import java.time.temporal.ChronoUnit;

/**
 * id, start_at, due_at, total_fee를 가져와 start_at과 due_at을 기간으로 바꾸어 ChallengeResult로 넘겨준다.
 */
public class ExpiredChallengeProcessor implements ItemProcessor<Challenge, ChallengeResult> {

    @Override
    public ChallengeResult process(Challenge challenge) throws Exception {
        int totalDays = (int) ChronoUnit.DAYS.between(challenge.getStartAt(), challenge.getDueAt()) + 1;

        return new ChallengeResult(challenge.getId(), challenge.getTotalFee(), totalDays);
    }
}
