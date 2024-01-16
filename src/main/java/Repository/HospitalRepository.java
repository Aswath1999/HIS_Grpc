package Repository;

import entities.HospitalModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<HospitalModel, Integer> {

}