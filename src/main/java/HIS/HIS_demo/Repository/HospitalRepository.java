package HIS.HIS_demo.Repository;

import HIS.HIS_demo.entities.HospitalModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<HospitalModel, Integer> {

}