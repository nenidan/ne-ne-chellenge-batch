package hello.batch.admin.model;

import hello.batch.admin.model.dto.type.DomainType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(
        name = "statistics_data",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_statistics_type_month",
                columnNames = {"type", "stat_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatisticData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private DomainType type;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate; // 해당 월의 앵커일(1일)

    @Lob @Column(name = "payload", columnDefinition = "json", nullable = false)
    private String payload; // 통계 JSON (도메인별 구조)

    @PrePersist @PreUpdate
    void normalizeMonth() {
        if (statDate != null) statDate = statDate.withDayOfMonth(1);
    }

    public static StatisticData of(DomainType type, YearMonth ym, String payloadJson) {
        StatisticData e = new StatisticData();
        e.type = type;
        e.statDate = ym.atDay(1);
        e.payload = payloadJson;
        return e;
    }

    public void updatePayload(String payloadJson) {
        this.payload = payloadJson;
    }
}
