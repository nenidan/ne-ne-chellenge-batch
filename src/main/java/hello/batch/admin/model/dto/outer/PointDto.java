package hello.batch.admin.model.dto.outer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PointDto {
    private Long pointTransactionId;

    private int amount;

    private String reason;

    private String description;
}
