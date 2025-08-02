package hello.batch.job.client;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

//@Component
//@Primary
public class TestPointClient implements PointClient {

    private final JdbcTemplate jdbcTemplate;

    public TestPointClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addPoint(Long userId, int point) {
        String sql = "UPDATE point_wallet SET balance = balance + ? WHERE id = ?";
        jdbcTemplate.update(sql, point, userId);
    }
}
