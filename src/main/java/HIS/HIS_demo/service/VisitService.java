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

@Service
public class VisitService {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private VisitAggregateRepository visitAggregateRepository;
    @Scheduled(cron = "0 0 0 1 * ?")
    public  void computeAndSaveVisitAggregates(int hospitalId) {
        // Fetch data from VisitModel based on hospitalId and calculate aggregates
        List<Object[]> aggregatedData = visitRepository.getAggregateDataByHospitalAndMonth(calculateStartDateForLast10Years(), hospitalId);

        // Process the aggregated data and save to VisitAggregateEntity
        for (Object[] data : aggregatedData) {
            int visitYear = ((Number) data[1]).intValue();
            int visitMonth = ((Number) data[2]).intValue();
            double averageAge = ((Number) data[3]).doubleValue();
            String gender = (String) data[4];

            VisitAggregateModel aggregateEntity = new VisitAggregateModel(hospitalId, visitYear, visitMonth, averageAge, gender);
            visitAggregateRepository.save(aggregateEntity);
        }
    }
    public List<VisitAggregateModel> getAggregatedDataByHospital(int hospitalId) {
        return visitAggregateRepository.findByHospitalId(hospitalId);
    }


    private Instant calculateStartDateForLast10Years() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -10);
        return calendar.getTime();
    }
}

