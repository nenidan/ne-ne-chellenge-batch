package hello.batch.job.finishstep;

import hello.batch.dto.Challenge;
import hello.batch.dto.ChallengeResult;
import org.springframework.batch.item.ItemProcessor;

import java.time.temporal.ChronoUnit;

/**
 * 간소화된 챌린지 정보를 받아와서 ChallengeResult 형태로 가공한다.
 */
public class ExpiredChallengeProcessor implements ItemProcessor<Challenge, ChallengeResult> {

    @Override
    public ChallengeResult process(Challenge challenge) throws Exception {
        int totalDays = (int) ChronoUnit.DAYS.between(challenge.getStartAt(), challenge.getDueAt()) + 1;

        return new ChallengeResult(challenge.getId(), challenge.getTotalFee(), totalDays);
    }
}
