package HIS.HIS_demo.entities;

import jakarta.persistence.*;
import java.time.Instant;
@Entity
public class VisitModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "patientid")
    private int patientId;

    @Column(name = "hospitalid")
    private int hospitalId;

    @Column(name = "age")
    private int age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "visit_date")
    private Instant visitDate;

    public VisitModel() {
    }

    public VisitModel(int patientId, int hospitalId, int age, String gender) {
        this.patientId = patientId;
        this.hospitalId = hospitalId;
        this.visitDate = Instant.now();
        this.age = age;
        this.gender = gender;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Instant getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Instant visitDate) {
        this.visitDate = visitDate;
    }
}
