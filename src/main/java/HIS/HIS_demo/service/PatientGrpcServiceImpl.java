package HIS.HIS_demo.service;
import HIS.HIS_demo.Repository.HospitalRepository;
import HIS.HIS_demo.Repository.PatientRepository;
import HIS.HIS_demo.entities.PatientModel;
import HIS.HIS_demo.entities.HospitalModel;
import patient.HospitalInfo;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.*;
import io.grpc.Status;
import HIS.HIS_demo.service.GrpcUtils;
import java.time.ZoneId;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;
import java.time.Instant;
import java.time.format.DateTimeFormatter;


@GrpcService
public class PatientGrpcServiceImpl extends PatientServiceGrpc.PatientServiceImplBase {
    // Repositories for database access
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    // Logger for logging information
    private static final Logger log = LoggerFactory.getLogger(PatientGrpcServiceImpl.class);
    // Constructor for dependency injection
    @Autowired
    public PatientGrpcServiceImpl(PatientRepository patientRepository,HospitalRepository hospitalRepository) {
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
    }
    // gRPC service method to create a new patient
    @Transactional
    @Override
    public void createPatient(CreatePatientRequest request, StreamObserver<PatientInfo> responseObserver) {
        try{
        PatientModel patientEntity = new PatientModel(
                request.getName(),
                request.getSex(),
                convertStringToDate(request.getDateOfBirth()),
                request.getAddress(),
                request.getPhoneNumber()
        );
        // Calculate age based on the date of birth
        patientEntity.calculateAge();
        // Save the new patient entity to the database
        PatientModel savedPatientEntity = patientRepository.save(patientEntity);
        // Map the saved patient entity to gRPC response and send it to the client
        PatientInfo response = mapToPatientInfo(savedPatientEntity);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }catch(Exception e) {
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL.withDescription("Error creating patient").asRuntimeException());
    }
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
    @Transactional
    @Override
    public void registerPatient(RegisterPatientRequest request, StreamObserver<PatientHospitalRegistrationResponse> responseObserver) {
        try {
            // Find the hospital entity by ID
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getHospitalId())
                    .orElseThrow(() -> new RuntimeException("Hospital not found"));
            // Find the patient entity by ID
            PatientModel patientEntity = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            // Check if the patient is already registered in the hospital
            if (hospitalEntity.getPatients().contains(patientEntity)) {
                throw new RuntimeException("Patient is already registered in the hospital");
            }
            // Register the patient in the hospital
            patientEntity.registerInHospital(hospitalEntity);
            // Save the updated patient and hospital entities to the database
            patientRepository.save(patientEntity);
            patientRepository.save(patientEntity);
            hospitalRepository.save(hospitalEntity);

            // Build and send the success response to the client
            PatientHospitalRegistrationResponse response = PatientHospitalRegistrationResponse.newBuilder()
                    .setPatientId(patientEntity.getId())
                    .setHospitalId(hospitalEntity.getId())
                    .setDateOfRegistration(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error during patient registration: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Error during patient registration").asRuntimeException());
        }
    }

    @Transactional
    @Override
    public void listHospitalsForPatient(ListHospitalsForPatientRequest request,
                                        StreamObserver<HospitalsList> responseObserver) {
        // Find the patient entity by ID
        try {
            PatientModel patientEntity = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

            // Get the list of hospitals associated with the patient
            List<HospitalModel> Patienthospitals = new ArrayList<>(patientEntity.getHospitals());

            // Map each hospital entity to a gRPC response
            List<patient.HospitalInfo> hospitalResponses = Patienthospitals.stream()
                    .map(this::mapToHospitalResponse)
                    .collect(Collectors.<patient.HospitalInfo>toList());

            // Build the gRPC response containing the list of hospitals
            HospitalsList hospitalsListResponse = HospitalsList.newBuilder()
                    .addAllHospitals(hospitalResponses)
                    .build();

            responseObserver.onNext(hospitalsListResponse);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            log.error("Error retrieving hospitals for patient. {}", ex.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            log.error("Error retrieving hospitals for patient. {}", ex.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    // Helper method to map a PatientModel entity to a PatientInfo gRPC response
    private PatientInfo mapToPatientInfo(PatientModel patientEntity) {
        return GrpcUtils.mapToPatientInfo(patientEntity);
    }
    private Instant convertStringToDate(String dateString) {
        try {
            return Instant.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }
    private patient.HospitalInfo mapToHospitalResponse(HospitalModel hospitalEntity) {
        return patient.HospitalInfo.newBuilder()
                .setId(hospitalEntity.getId())
                .setName(hospitalEntity.getName())
                .setLocation(hospitalEntity.getLocation())
                .setNumberOfBeds(hospitalEntity.getNumber_of_beds())
                .setFoundingDate(hospitalEntity.getFounding_date())
                .build();
    }

}
