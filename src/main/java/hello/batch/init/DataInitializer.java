package hello.batch.init;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataInitializer {

    private final JdbcTemplate jdbcTemplate;

    public static final LocalDate TARGET_DATE = LocalDate.of(2025,8,1);

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void 멀티스레드_챌린지_삽입(int size) throws InterruptedException {
        String SQL = """
            INSERT INTO challenge (
                due_at, max_participants, min_participants,
                participation_fee, start_at, total_fee,
                created_at, deleted_at,
                description, name, category, status, id, current_participant_count
                    )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        int threadCount = 8;
        int batchSize = 1000;
        int chunkSize = size / threadCount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            int start = t * chunkSize + 1;
            int end = (t == threadCount - 1) ? size : (t + 1) * chunkSize;

            executor.submit(() -> {
                try {
                    List<Object[]> batchArgs = new ArrayList<>();
                    for (int i = start; i <= end; i++) {
                        addRandom마감ChallengeInBatchArgs(i, batchArgs);

                        if (batchArgs.size() >= batchSize) {
                            jdbcTemplate.batchUpdate(SQL, batchArgs);
                            batchArgs.clear();
                        }
                    }
                    // 남은 데이터 처리
                    if (!batchArgs.isEmpty()) {
                        jdbcTemplate.batchUpdate(SQL, batchArgs);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    public void 마감할_챌린지_삽입(String SQL, int size) {
        final int BATCH_SIZE = 1000; // 적절한 값으로 조절 (1000~5000 추천)

        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= size; i++) {
            addRandom마감ChallengeInBatchArgs(i, batchArgs);

            if (batchArgs.size() >= BATCH_SIZE) {
                jdbcTemplate.batchUpdate(SQL, batchArgs);
                batchArgs.clear();
            }
        }

        // 남은 데이터 처리
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(SQL, batchArgs);
        }
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM history");
        jdbcTemplate.update("DELETE FROM point_transaction");
        jdbcTemplate.update("DELETE FROM point_wallet");
        jdbcTemplate.update("DELETE FROM challenge");
    }

    private void addRandom마감ChallengeInBatchArgs(int i, List<Object[]> batchArgs) {
        String name = "challenge" + i;
        String description = "description" + i;
        String category = pickRandom(List.of("HABIT", "HEALTH", "RELATIONSHIP", "STUDY"));
        int minParticipants = ThreadLocalRandom.current().nextInt(1, 5);
        int maxParticipants = ThreadLocalRandom.current()
            .nextInt(minParticipants + 1, minParticipants + 6);
        int participationFee = ThreadLocalRandom.current().nextInt(1000, 5001);
        int numberOfParticipants = ThreadLocalRandom.current().nextInt(minParticipants, maxParticipants + 1);
        int totalFee = participationFee * numberOfParticipants;
        LocalDate startAt = DataInitializer.TARGET_DATE.minusDays(ThreadLocalRandom.current().nextInt(1, 41));
        String status = "ONGOING";

        batchArgs.add(new Object[]{
            Date.valueOf(DataInitializer.TARGET_DATE),
            maxParticipants,
            minParticipants,
            participationFee,
            Date.valueOf(startAt),
            totalFee,
            Timestamp.valueOf(startAt.atStartOfDay()),
            null,
            description,
            name,
            category,
            status,
            i,
            numberOfParticipants
        });
    }

    private <T> T pickRandom(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
