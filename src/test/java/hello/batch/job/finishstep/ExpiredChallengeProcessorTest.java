package hello.batch.job.finishstep;

import hello.batch.model.Challenge;
import hello.batch.model.ChallengeResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ExpiredChallengeProcessorTest {

    ExpiredChallengeProcessor processor = new ExpiredChallengeProcessor();

    @Test
    void process() throws Exception {
        // given
        Challenge challenge = new Challenge(1L, LocalDate.of(2025, 7, 2), LocalDate.of(2025, 7, 15), 20000);

        // when
        ChallengeResult challengeResult = processor.process(challenge);

        // then
        assertThat(Objects.requireNonNull(challengeResult).getChallengeId()).isEqualTo(1L);
        assertThat(challengeResult.getTotalDays()).isEqualTo(14L);
        assertThat(challengeResult.getTotalFee()).isEqualTo(20000);
    }
}