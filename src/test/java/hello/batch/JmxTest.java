package hello.batch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class JmxTest {

    @Test
    void waits() throws InterruptedException {
        Thread.sleep(600_000); // 10분 대기
    }
}
