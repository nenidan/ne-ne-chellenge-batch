package hello.batch.admin.model.dto.outer;

import hello.batch.admin.model.dto.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class PaymentDto {
    private Long id;

    private Long userId;

    private int amount;

    private String paymentMethod;

    private String paymentKey;

    private String orderId;

    private PaymentStatus status;

    private String cancelReason;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime failedAt;

    private LocalDateTime canceledAt;
}
