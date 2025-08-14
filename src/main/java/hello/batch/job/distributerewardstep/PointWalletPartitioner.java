package hello.batch.job.distributerewardstep;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PointWalletPartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;

    public PointWalletPartitioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Long minUserId = jdbcTemplate.queryForObject("SELECT MIN(user_id) FROM tmp_reward_info", Long.class);
        Long maxUserId = jdbcTemplate.queryForObject("SELECT MAX(user_id) FROM tmp_reward_info", Long.class);

        long range = (maxUserId - minUserId + 1) / gridSize;
        long start = minUserId;
        long end;

        Map<String, ExecutionContext> partitions = new HashMap<>();
        for (int i = 0; i < gridSize; i++) {
            end = (i == gridSize - 1) ? maxUserId : start + range - 1;
            ExecutionContext context = new ExecutionContext();
            context.putLong("minUserId", start);
            context.putLong("maxUserId", end);
            String partitionName = "partition-" + i + "[" + start + "-" + end + "]";
            context.putString("partition.name", partitionName);
            partitions.put("partition" + i, context);
            start = end + 1;
        }

        return partitions;
    }
}
