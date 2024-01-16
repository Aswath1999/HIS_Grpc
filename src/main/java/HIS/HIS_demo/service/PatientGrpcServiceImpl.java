package HIS.HIS_demo.service;

import HIS.HIS_demo.Repository.PatientRepository;
import HIS.HIS_demo.entities.PatientModel;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.*;
import io.grpc.Status;

import java.time.ZoneId;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class PatientGrpcServiceImpl extends PatientServiceGrpc.PatientServiceImplBase {

    private final PatientRepository patientRepository;
    private static final Logger log = LoggerFactory.getLogger(PatientGrpcServiceImpl.class);

    @Autowired
    public PatientGrpcServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    @Override
    public void createPatient(CreatePatientRequest request, StreamObserver<PatientInfo> responseObserver) {
        PatientModel patientEntity = new PatientModel(
                request.getName(),
                request.getSex(),
                convertStringToDate(request.getDateOfBirth()),
                request.getAddress(),
                request.getPhoneNumber()
        );

        // Explicitly calculate age before saving
        patientEntity.calculateAge();

        PatientModel savedPatientEntity = patientRepository.save(patientEntity);

        PatientInfo response = mapToPatientInfo(savedPatientEntity);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void updatePatient(UpdatePatientRequest request, StreamObserver<PatientInfo> responseObserver) {
        try {
            PatientModel patientEntity = patientRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

            // Update patient details
            patientEntity.setName(request.getName());
            patientEntity.setSex(request.getSex());
            patientEntity.setDateOfBirth(convertStringToDate(request.getDateOfBirth()));
            patientEntity.setAddress(request.getAddress());
            patientEntity.setPhoneNumber(request.getPhoneNumber());
            patientEntity.calculateAge();

            // Save the updated patient
            PatientModel updatedPatientEntity = patientRepository.save(patientEntity);

            // Build and send the updated patient response
            PatientInfo response = mapToPatientInfo(updatedPatientEntity);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Patient not found").asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }


    @Transactional
    @Override
    public void deletePatient(DeletePatientRequest request, StreamObserver<PatientInfo> responseObserver) {
        try {
            PatientModel patientEntity = patientRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

            // Delete the patient
            patientRepository.delete(patientEntity);

            PatientInfo deletedPatientResponse = mapToPatientInfo(patientEntity);

            responseObserver.onNext(deletedPatientResponse);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Patient not found").asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Transactional
    @Override
    public void listPatients(ListPatientsRequest request, StreamObserver<PatientListResponse> responseObserver) {
        try {
            List<PatientModel> patients = patientRepository.findAll();

            List<PatientInfo> patientResponses = patients.stream()
                    .map(this::mapToPatientInfo)
                    .collect(Collectors.toList());

            PatientListResponse patientsListResponse = PatientListResponse.newBuilder()
                    .addAllPatients(patientResponses)
                    .build();

            responseObserver.onNext(patientsListResponse);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    private PatientInfo mapToPatientInfo(PatientModel patientEntity) {
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
    private Instant convertStringToDate(String dateString) {
        try {
            // Assuming dateString is in ISO 8601 format
            return Instant.parse(dateString);
        } catch (DateTimeParseException e) {
            // Handle parsing error
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }
}