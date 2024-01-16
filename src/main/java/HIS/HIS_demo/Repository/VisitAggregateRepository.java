package HIS.HIS_demo.Repository;

import HIS.HIS_demo.entities.VisitAggregateModel;
import HIS.HIS_demo.entities.VisitModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitAggregateRepository extends JpaRepository<VisitAggregateModel, Integer> {

}
