package hello.batch.admin.repository;

import hello.batch.admin.model.StatisticData;
import hello.batch.admin.model.dto.type.DomainType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StatisticTransactionRepository extends JpaRepository<StatisticData, Long>{
    Optional<StatisticData> findByTypeAndStatDate(DomainType type, LocalDate statDate);
}
