package hello.batch.model;

import java.time.LocalDate;

public class Challenge {

    private final Long id;

    private final LocalDate startAt;

    private final LocalDate dueAt;

    private final Integer totalFee;

    public Challenge(Long id, LocalDate startAt, LocalDate dueAt, Integer totalFee) {
        this.id = id;
        this.startAt = startAt;
        this.dueAt = dueAt;
        this.totalFee = totalFee;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getStartAt() {
        return startAt;
    }

    public LocalDate getDueAt() {
        return dueAt;
    }

    public Integer getTotalFee() {
        return totalFee;
    }
}
