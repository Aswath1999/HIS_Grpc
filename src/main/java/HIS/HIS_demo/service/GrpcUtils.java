package HIS.HIS_demo.service;

import HIS.HIS_demo.entities.HospitalModel;
import HIS.HIS_demo.entities.PatientModel;
import com.google.protobuf.Timestamp;
import patient.PatientInfo;
import hospital.HospitalInfo;


public class GrpcUtils {
    public static PatientInfo mapToPatientInfo(PatientModel patientEntity) {
        return PatientInfo.newBuilder()
                .setId(patientEntity.getId())
                .setName(patientEntity.getName())
                .setSex(patientEntity.getSex())
                .setDateOfBirth(Timestamp.newBuilder().setSeconds(patientEntity.getDateOfBirth().getEpochSecond()))
                .setAddress(patientEntity.getAddress())
                .setPhoneNumber(patientEntity.getPhoneNumber())
                .setAge(patientEntity.getAge())
                .build();
    }
    public static HospitalInfo mapToHospitalResponse(HospitalModel hospitalEntity) {
        return HospitalInfo.newBuilder()
                .setId(hospitalEntity.getId())
                .setName(hospitalEntity.getName())
                .setLocation(hospitalEntity.getLocation())
                .setNumberOfBeds(hospitalEntity.getNumber_of_beds())
                .setFoundingDate(hospitalEntity.getFounding_date())
                .build();
    }
}