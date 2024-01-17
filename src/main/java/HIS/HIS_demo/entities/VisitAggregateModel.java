package HIS.HIS_demo.entities;
import jakarta.persistence.*;


@Entity
@Table(name = "visit_aggregate", indexes = {
        @Index(name = "idx_hospital_id", columnList = "hospital_id"),
        @Index(name = "idx_gender", columnList = "gender"),
        @Index(name = "idx_age", columnList = "average_age"),
        @Index(name = "idx_year_month", columnList = "visit_year, visit_month")
})
public class VisitAggregateModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "hospital_id")
    private int hospitalId;

    @Column(name = "visit_year")
    private int visitYear;

    @Column(name = "visit_month")
    private int visitMonth;

    @Column(name = "average_age")
    private int averageAge;

    @Column(name = "gender")
    private String gender;

    public VisitAggregateModel() {
    }

    public VisitAggregateModel(int hospitalId, int visitYear, int visitMonth, double averageAge, String gender) {
        this.hospitalId = hospitalId;
        this.visitYear = visitYear;
        this.visitMonth = visitMonth;
        this.averageAge = (int) Math.round(averageAge);
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }

    public int getVisitYear() {
        return visitYear;
    }

    public void setVisitYear(int visitYear) {
        this.visitYear = visitYear;
    }

    public int getVisitMonth() {
        return visitMonth;
    }

    public void setVisitMonth(int visitMonth) {
        this.visitMonth = visitMonth;
    }

    public int getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(double averageAge) {
        this.averageAge = (int) Math.round(averageAge);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
