package HIS.HIS_demo.service;

import HIS.HIS_demo.Repository.VisitAggregateRepository;
import HIS.HIS_demo.Repository.VisitRepository;
import HIS.HIS_demo.entities.VisitAggregateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VisitService {
    private static final Logger log = LoggerFactory.getLogger(VisitService.class);
    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private VisitAggregateRepository visitAggregateRepository;

    public void computeAndSaveVisitAggregates(int hospitalId) {
        try {
            Instant startDate = calculateStartDateForLast10Years();
            Instant enddate= Instant.now();
            log.info("Start date for computation: {}", startDate);

            // Fetch data from VisitModel based on hospitalId and calculate aggregates
            List<Object[]> aggregatedData = visitRepository.getAggregateDataByHospitalAndMonth(startDate, hospitalId,enddate);
            log.info("Number of aggregated data fetched: {}", aggregatedData.size());
            log.info("Executing query with hospitalId: {}", hospitalId);
            // Process the aggregated data and save to VisitAggregateEntity
            for (Object[] data : aggregatedData) {
                int visitYear = ((Number) data[1]).intValue();
                int visitMonth = ((Number) data[2]).intValue();
                double averageAge = ((Number) data[3]).doubleValue();
                String gender = (String) data[4];

                VisitAggregateModel aggregateEntity = new VisitAggregateModel(hospitalId, visitYear, visitMonth, averageAge, gender);
                visitAggregateRepository.save(aggregateEntity);
                log.info("Visit aggregates computation and save completed successfully for hospitalId: {}", hospitalId);
            }
        }

        catch (Exception e){
            log.error("Error computing and saving visit aggregates for hospitalId: {}", hospitalId, e);
        }
    }

    public List<VisitAggregateModel> getAggregatedDataByHospital(int hospitalId) {
        return visitAggregateRepository.findByHospitalId(hospitalId);
    }


    private Instant calculateStartDateForLast10Years() {
        return Instant.now().minusSeconds(10L * 365 * 24 * 60 * 60); // 10 years ago
    }
}

