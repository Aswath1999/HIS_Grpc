package HIS.HIS_demo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Patient_model", indexes = {
        @Index(name = "idx_dateOfBirth", columnList = "dateOfBirth"),
        @Index(name = "idx_sex", columnList = "sex")
})
public class PatientModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "sex", length = 6, nullable = false)
    private String sex;

    @Column(name = "dateOfBirth", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant dateOfBirth;

    @Column(name = "age")
    private int age;

    @Column(name = "address", length = 30, nullable = false)
    private String address;

    @Column(name = "phoneNumber", length = 10)
    private String phoneNumber;

    @ManyToMany(mappedBy = "patients")
    private Set<HospitalModel> registeredHospitals = new HashSet<>();

    public PatientModel() {
    }

    public PatientModel(
            String name,
            String sex,
            Instant dateOfBirth,
            String address,
            String phoneNumber
    ){
        this.name = name;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.age = calculateAgeFromDateOfBirth(dateOfBirth);
        this.address = address;
        this.phoneNumber = phoneNumber;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Instant dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<HospitalModel> getRegisteredHospitals() {
        return registeredHospitals;
    }

    public void registerInHospital(HospitalModel hospital) {
        registeredHospitals.add(hospital);
        hospital.getPatients().add(this);
    }


    public void unregisterFromHospital(HospitalModel hospital) {
        registeredHospitals.remove(hospital);
        hospital.getPatients().remove(this);
    }

    @AssertTrue(message = "Invalid date of birth")
    public boolean isValidDateOfBirth() {
        return dateOfBirth != null && !dateOfBirth.isAfter(Instant.now());
    }

    private int calculateAgeFromDateOfBirth(Instant dateOfBirth) {
        return Period.between(
                LocalDateTime.ofInstant(dateOfBirth, ZoneId.systemDefault()).toLocalDate(),
                LocalDate.now()
        ).getYears();
    }


    public void calculateAge() {
        if (dateOfBirth != null) {
            age = calculateAgeFromDateOfBirth(dateOfBirth);
        }
    }
}
