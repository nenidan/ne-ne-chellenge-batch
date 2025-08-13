package hello.batch.admin.model.dto.type;

import java.util.Arrays;

public enum PaymentStatus {
    DONE, FAIL, CANCELED;

    public static PaymentStatus of(String status) {
        return Arrays.stream(PaymentStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("유효하지 않은 결제 상태입니다."));
    }
}
