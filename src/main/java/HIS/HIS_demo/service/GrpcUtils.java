package HIS.HIS_demo.service;

import HIS.HIS_demo.entities.PatientModel;
import com.google.protobuf.Timestamp;
import patient.PatientInfo;


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
}