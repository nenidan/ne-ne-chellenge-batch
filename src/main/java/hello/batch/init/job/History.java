package hello.batch.init.job;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class History {
    private Long userId;
    private Long challengeId;
    private boolean isSuccess;
    private String content;
    private LocalDate date;
    private LocalDateTime createdAt;

    public History(Long userId, Long challengeId, boolean isSuccess, String content, LocalDate date,
        LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.isSuccess = isSuccess;
        this.content = content;
        this.date = date;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
