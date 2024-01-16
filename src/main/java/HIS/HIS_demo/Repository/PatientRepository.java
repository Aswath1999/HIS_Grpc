package HIS.HIS_demo.Repository;
import HIS.HIS_demo.entities.PatientModel;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PatientRepository  extends JpaRepository<PatientModel, Integer>{
}
