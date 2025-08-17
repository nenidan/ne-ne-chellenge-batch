package hello.batch.admin.job;

import hello.batch.admin.model.dto.ChallengeStatisticsResponse;
import hello.batch.admin.model.dto.PaymentStatisticsResponse;
import hello.batch.admin.model.dto.PointStatisticsResponse;
import hello.batch.admin.model.dto.UserStatisticsResponse;
import hello.batch.admin.service.StatisticsCalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/batch/statistics")
@RequiredArgsConstructor
public class StatisticsBatchController { // 웹 서버에서 조회시 redis 서버가 재구동 및 최초 조회일 경우 api 호출.

    private final StatisticsCalService statisticsCalService;

    @GetMapping("/challenge")
    public ChallengeStatisticsResponse runChallengeStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthPeriod) {
        return statisticsCalService.getChallengeStatistics(monthPeriod);
    }

    @GetMapping("/payment")
    public PaymentStatisticsResponse runPaymentStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthPeriod) {
        return statisticsCalService.getPaymentStatistics(monthPeriod);
    }

    @GetMapping("/point")
    public PointStatisticsResponse runPointStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthPeriod) {
        return statisticsCalService.getPointStatistics(monthPeriod);
    }

    @GetMapping("/user")
    public UserStatisticsResponse runUserStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthPeriod) {
        return statisticsCalService.getUserStatistics(monthPeriod);
    }

}
