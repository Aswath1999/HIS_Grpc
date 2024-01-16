package HIS.HIS_demo.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class VisitModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Basic
    @Column(name = "patient_id", insertable = false, updatable = false)
    private int patientId;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private PatientModel patient;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private HospitalModel hospital;

    @Basic
    @Column(name = "hospital_id", insertable = false, updatable = false)
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
        this.age=age;
        this.gender=gender;
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

    public PatientModel getPatient() {
        return patient;
    }

    public void setPatient(PatientModel patient) {
        this.patient = patient;
    }

    public HospitalModel getHospital() {
        return hospital;
    }

    public void setHospital(HospitalModel hospital) {
        this.hospital = hospital;
    }

    public int getAge() {
        return age;
    }


    public void setAge(int age) {
        this.age = age;
    }
    public int getHospitalId() {
        return hospitalId;
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

