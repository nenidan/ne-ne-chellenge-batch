package hello.batch.admin.service;

import hello.batch.admin.client.ChallengeClientPort;
import hello.batch.admin.client.PaymentClientPort;
import hello.batch.admin.client.PointClientPort;
import hello.batch.admin.client.UserClientPort;
import hello.batch.admin.model.dto.ChallengeStatisticsResponse;
import hello.batch.admin.model.dto.PaymentStatisticsResponse;
import hello.batch.admin.model.dto.PointStatisticsResponse;
import hello.batch.admin.model.dto.UserStatisticsResponse;
import hello.batch.admin.model.dto.outer.*;
import hello.batch.admin.model.dto.type.ChallengeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticsCalService {

    private final ChallengeClientPort challengeClientPort;
    private final PaymentClientPort paymentClientPort;
    private final PointClientPort pointClientPort;
    private final UserClientPort userClientPort;

    public ChallengeStatisticsResponse getChallengeStatistics(LocalDateTime monthPeriod) {

        List<ChallengeDto> challengeList = challengeClientPort.getAllChallenges();
        List<ChallengeUserDto> challengeUserList = challengeClientPort.getAllChallengeUsers();

        // 일별 참가자 수 계산
        Map<LocalDate, Long> dailyParticipants = challengeUserList.stream()
                .collect(Collectors.groupingBy(
                        cu -> cu.getCreatedAt().toLocalDate(), // 참여한 날짜 기준
                        Collectors.counting()
                ));

        // 월별 참가자 수 계산
        Map<YearMonth, Long> monthlyParticipants = challengeUserList.stream()
                .collect(Collectors.groupingBy(
                        cu -> YearMonth.from(cu.getCreatedAt()),
                        Collectors.counting()
                ));

        // 챌린지 참여율 (시작된 챌린지 수 / 전체 챌린지 수)
        long startedCount = challengeList.stream()
                .filter(c -> c.getStatus() != null)
                .filter(c -> EnumSet.of(ChallengeStatus.ONGOING, ChallengeStatus.FINISHED).contains(c.getStatus()))
                .count();

        double participationRate = challengeList.isEmpty() ? 0.0 :
                (double) startedCount / challengeList.size();

        return new ChallengeStatisticsResponse("challenge",
                monthPeriod,
                dailyParticipants,
                monthlyParticipants,
                participationRate
        );

    }


    public PaymentStatisticsResponse getPaymentStatistics(LocalDateTime monthPeriod) {
        List<PaymentDto> paymentList = paymentClientPort.getAllPayments();

        // 입력된 월에 해당하는 결제만 필터링
        YearMonth targetMonth = YearMonth.from(monthPeriod);

        List<PaymentDto> filteredPayments = paymentList.stream()
                .filter(p -> p.getApprovedAt() != null)
                .filter(p -> YearMonth.from(p.getApprovedAt()).equals(targetMonth))
                .collect(Collectors.toList());

        int count = filteredPayments.size();
        double avgAmount = count == 0 ? 0.0 :
                filteredPayments.stream().mapToInt(PaymentDto::getAmount).average().orElse(0.0);

        return new PaymentStatisticsResponse("payment", monthPeriod, count, avgAmount);
    }

    public PointStatisticsResponse getPointStatistics(LocalDateTime monthPeriod) {
        List<PointDto> pointList = pointClientPort.getAllPoint();

        if (pointList.isEmpty()) {
            return new PointStatisticsResponse("point", monthPeriod, 0.0, 0, "none");
        }

        // 이유별로 그룹핑
        Map<String, Long> reasonCountMap = pointList.stream()
                .collect(Collectors.groupingBy(PointDto::getReason, Collectors.counting()));

        // 가장 많이 발생한 이유 기준으로 비율 계산
        Map.Entry<String, Long> topReasonEntry = reasonCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();

        String topReason = topReasonEntry.getKey();
        long topCnt = topReasonEntry.getValue();
        long totalCnt = pointList.size();

        double rate = (double) topCnt / totalCnt;

        return new PointStatisticsResponse("point", monthPeriod, rate, (int) topCnt, topReason);
    }

    public UserStatisticsResponse getUserStatistics(LocalDateTime monthPeriod) {
        List<UserDto> userList = userClientPort.getAllUsers();

        YearMonth targetMonth = YearMonth.from(monthPeriod);

        // 신규 회원 수 (해당 월 가입자)
        long newUsersCount = userList.stream()
                .filter(u -> u.getCreatedAt() != null)
                .filter(u -> YearMonth.from(u.getCreatedAt()).equals(targetMonth))
                .count();

        // 총 회원 수 (탈퇴 제외)
        long totalUsersCount = userList.stream()
                .filter(u -> u.getDeletedAt() == null) // 탈퇴 여부 판단
                .count();

        return new UserStatisticsResponse(
                "user",
                monthPeriod,
                newUsersCount,
                totalUsersCount
        );
    }
}
