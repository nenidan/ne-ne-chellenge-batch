package hello.batch.job.init.sql;

import hello.batch.job.init.dto.ChallengeDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SqlUtil {

    private final JdbcTemplate jdbcTemplate;

    public SqlUtil(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertChallenge(List<ChallengeDto> dto) {

    }
}
