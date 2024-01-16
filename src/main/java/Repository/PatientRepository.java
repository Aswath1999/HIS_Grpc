package Repository;
import entities.HospitalModel;
import entities.PatientModel;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PatientRepository  extends JpaRepository<PatientModel, Integer>{
}
