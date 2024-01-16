package HIS.HIS_demo.entities;
import jakarta.persistence.*;


@Entity
@Table(name = "visit_aggregate")
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

    // Constructors

    public VisitAggregateModel() {
    }

    public VisitAggregateModel(int hospitalId, int visitYear, int visitMonth, double averageAge, String gender) {
        this.hospitalId = hospitalId;
        this.visitYear = visitYear;
        this.visitMonth = visitMonth;
        this.averageAge = (int) Math.round(averageAge);
        this.gender = gender;
    }

    // Getters and setters

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
