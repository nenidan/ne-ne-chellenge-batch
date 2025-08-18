package hello.batch.admin.model.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointStatsPayload {
    private double reasonRate; private int cnt; private String reason;
}
