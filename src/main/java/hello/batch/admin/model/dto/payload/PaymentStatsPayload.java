package hello.batch.admin.model.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsPayload {
    private int count;
    private double avgAmount;
}
