package hello.batch.admin.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentStatisticsResponse extends StatisticsResponse{
    private int count;           // 결제 건수
    private double avgAmount;   // 금액 평균

    public PaymentStatisticsResponse(String type, LocalDateTime createdAt, int count, double avgAmount) {
        super(type, createdAt);
        this.count = count;
        this.avgAmount = avgAmount;
    }
}
