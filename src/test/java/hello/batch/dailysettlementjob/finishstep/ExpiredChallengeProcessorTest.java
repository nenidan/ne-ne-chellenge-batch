package hello.batch.dailysettlementjob.finishstep;

import hello.batch.dailysettlementjob.dto.Challenge;
import hello.batch.dailysettlementjob.dto.ChallengeResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
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