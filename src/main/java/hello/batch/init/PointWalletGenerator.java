package hello.batch.init;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PointWalletGenerator {

    private final JdbcTemplate jdbcTemplate;

    private static final int TOTAL_USERS = 1_000_000;
    private static final int THREAD_COUNT = 5;
    private static final int BATCH_SIZE = 10_000;

    public PointWalletGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertMillionUsersInParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        int rangePerThread = TOTAL_USERS / THREAD_COUNT;

        for (int i = 0; i < THREAD_COUNT; i++) {
            int start = i * rangePerThread + 1;
            int end = (i == THREAD_COUNT - 1) ? TOTAL_USERS : (i + 1) * rangePerThread;

            executor.submit(() -> insertRange(start, end));
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // 대기
        }

        System.out.println("✅ 멀티스레드로 100만 건 삽입 완료");
    }

    private void insertRange(int start, int end) {
        String sql = "INSERT INTO point_wallet (user_id, balance, created_at) VALUES (?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            batchArgs.add(new Object[]{i, 0, now});

            if (batchArgs.size() >= BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        System.out.printf("Thread %s: 완료 (%d ~ %d)%n", Thread.currentThread().getName(), start, end);
    }
}
