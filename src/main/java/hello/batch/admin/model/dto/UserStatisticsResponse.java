package hello.batch.admin.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserStatisticsResponse extends StatisticsResponse{
   private Long newUsersCount;
   private Long totalUsersCount;

    public UserStatisticsResponse(String type, LocalDateTime createdAt, Long newUsersCount, Long totalUsersCount) {
        super(type, createdAt);
        this.newUsersCount = newUsersCount;
        this.totalUsersCount = totalUsersCount;
    }
}
