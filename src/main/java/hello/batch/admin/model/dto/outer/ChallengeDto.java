package hello.batch.admin.model.dto.outer;


import hello.batch.admin.model.dto.type.ChallengeCategory;
import hello.batch.admin.model.dto.type.ChallengeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ChallengeDto{
    private Long id;

    private String name;

    private String description;

    private ChallengeStatus status;

    private ChallengeCategory category;

    private int minParticipants;

    private int maxParticipants;

    private int participationFee;

    private int totalFee;

    private LocalDate startAt;

    private LocalDate dueAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
