package hello.batch.admin.model.dto.outer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ChallengeUserDto {
    private Long id;

    private Long userId;

    private Long challengeId;

    private boolean isHost;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
