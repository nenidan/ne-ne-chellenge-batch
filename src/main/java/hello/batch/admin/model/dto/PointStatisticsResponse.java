package hello.batch.admin.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointStatisticsResponse extends StatisticsResponse {
    private double ReasonRate;
    private int cnt;
    private String reason;

    public PointStatisticsResponse(String type, LocalDateTime createdAt, double ReasonRate, int cnt, String reason) {
        super(type, createdAt);
        this.ReasonRate = ReasonRate;
        this.cnt = cnt;
        this.reason = reason;
    }
}
