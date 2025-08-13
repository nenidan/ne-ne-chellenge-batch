package hello.batch.launch;

import hello.batch.init.DataInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ChallengeGenerator {

    @Autowired
    DataInitializer dataInitializer;

//    @Test
    void generateChallenge() throws InterruptedException {
        dataInitializer.멀티스레드_챌린지_삽입(10_000);
    }
}
