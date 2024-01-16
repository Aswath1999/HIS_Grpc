package entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
@Entity
public class HospitalModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String location;
    private int number_of_beds;
    private String founding_date;
    public HospitalModel() {
    }

    public HospitalModel(String name, String location, int number_of_beds, String founding_date) {
        this.name = name;
        this.location = location;
        this.number_of_beds = number_of_beds;
        this.founding_date = founding_date;
    }
    @ManyToMany
    @JoinTable(
            name = "patient_hospital_registration",
            joinColumns = @JoinColumn(name = "hospital_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private Set<PatientModel> patients = new HashSet<>();

    public Set<PatientModel> getPatients() {
        return patients;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getNumber_of_beds() {
        return number_of_beds;
    }

    public void setNumber_of_beds(int number_of_beds) {
        this.number_of_beds = number_of_beds;
    }

    public String getFounding_date() {
        return founding_date;
    }

    public void setFounding_date(String founding_date) {
        this.founding_date = founding_date;
    }

}
