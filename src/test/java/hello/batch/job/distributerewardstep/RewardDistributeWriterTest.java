package hello.batch.job.distributerewardstep;

import hello.batch.dto.RewardInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RewardDistributeWriterTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ItemWriter<RewardInfo> writer;

    @BeforeEach
    void setUp() {
        // 1, 2번 유저 포인트 지갑 주입, point_wallet_id 찾는 private 메소드 검증 위해 id를 100과 200으로 설정
        jdbcTemplate.update(
            "INSERT INTO point_wallet (id, balance, user_id, created_at) " +
                "VALUES (100, 0, 1, '2025-08-01'), (200, 0, 2, '2025-08-01')");

        // 임시 테이블에 아직 정보가 남아있음
        jdbcTemplate.update(
            "INSERT INTO tmp_reward_info (user_id, challenge_id, amount) " +
                "VALUES (1, 1, 1000), (1, 2, 2000), (2, 1, 1500)");
    }

    @Test
    void distributeReward() throws Exception {
        // given
        RewardInfo rewardInfo1 = new RewardInfo(1L, 1L, 1000);
        RewardInfo rewardInfo2 = new RewardInfo(2L, 1L, 2000);
        RewardInfo rewardInfo3 = new RewardInfo(1L, 2L, 1500);
        Chunk<RewardInfo> chunk = new Chunk<>(List.of(rewardInfo1, rewardInfo2, rewardInfo3));

        // when
        writer.write(chunk);

        // then
        Integer tmpTableCount = jdbcTemplate.queryForObject("SELECT count(*) FROM tmp_reward_info", Integer.class);
        assertThat(tmpTableCount).isEqualTo(0);

        Integer user1Balance = jdbcTemplate.queryForObject("SELECT balance FROM point_wallet WHERE user_id = 1", Integer.class);
        assertThat(user1Balance).isEqualTo(3000);

        Integer user2Balance = jdbcTemplate.queryForObject("SELECT balance FROM point_wallet WHERE user_id = 2", Integer.class);
        assertThat(user2Balance).isEqualTo(1500);

        Integer transactionCount = jdbcTemplate.queryForObject("SELECT count(*) FROM point_transaction", Integer.class);
        assertThat(transactionCount).isEqualTo(3);
    }
}