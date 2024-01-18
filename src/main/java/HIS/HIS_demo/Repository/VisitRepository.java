package HIS.HIS_demo.Repository;

import HIS.HIS_demo.entities.HospitalModel;
import HIS.HIS_demo.entities.VisitModel;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public interface VisitRepository extends JpaRepository<VisitModel, Integer> {


    @Query("SELECT v.hospital.id, EXTRACT(YEAR FROM v.visitDate), EXTRACT(MONTH FROM v.visitDate), AVG(v.age) as avgAge, v.gender " +
            "FROM VisitModel v " +
            "WHERE v.visitDate >= :startDate AND v.hospital.id = :hospitalId  AND v.visitDate <= :endDate" +
            "GROUP BY v.hospital.id, EXTRACT(YEAR FROM v.visitDate), EXTRACT(MONTH FROM v.visitDate), v.gender")
    List<Object[]> getAggregateDataByHospitalAndMonth(@Param("startDate") Instant startDate, @Param("hospitalId") int hospitalId, @Param("endDate")Instant endDate);


}

