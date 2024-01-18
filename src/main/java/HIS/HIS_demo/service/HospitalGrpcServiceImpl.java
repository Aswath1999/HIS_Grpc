package HIS.HIS_demo.service;
import HIS.HIS_demo.Repository.HospitalRepository;
import HIS.HIS_demo.Repository.PatientRepository;
import HIS.HIS_demo.Repository.VisitAggregateRepository;
import HIS.HIS_demo.Repository.VisitRepository;
import HIS.HIS_demo.entities.HospitalModel;
import HIS.HIS_demo.entities.PatientModel;
import HIS.HIS_demo.entities.VisitAggregateModel;
import HIS.HIS_demo.entities.VisitModel;
import HIS.HIS_demo.service.GrpcUtils;
import com.google.protobuf.Timestamp;
import patient.PatientInfo;
import hospital.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class HospitalGrpcServiceImpl extends HospitalServiceGrpc.HospitalServiceImplBase{
    // Repositories for database access
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final VisitAggregateRepository visitAggregateRepository;
    // Service for additional visit-related operations
    @Autowired
    private final VisitService visitService;
    // Logger for logging information
    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcServiceImpl.class);
    // Constructor for dependency injection
    @Autowired
    public HospitalGrpcServiceImpl(HospitalRepository hospitalRepository,
                                   PatientRepository patientRepository,
                                   VisitRepository visitRepository,
                                   VisitAggregateRepository visitAggregateRepository,
                                   VisitService visitService) {
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.visitRepository = visitRepository;
        this.visitService = visitService;
        this.visitAggregateRepository=visitAggregateRepository;
    }
    @Transactional
    @Override
    public void createHospital(CreateHospitalRequest request, StreamObserver<HospitalInfo> responseObserver) {
        // Create a new HospitalModel instance from the gRPC request
        HospitalModel hospitalEntity = new HospitalModel(
                request.getName(),
                request.getLocation(),
                request.getNumberOfBeds(),
                request.getFoundingDate()
        );
        // Save the new hospital entity to the database

        HospitalModel savedHospitalEntity = hospitalRepository.save(hospitalEntity);

        log.info("Hospital created - ID: {}, Name: {}", savedHospitalEntity.getId(), savedHospitalEntity.getName());
        // Map the saved hospital entity to gRPC response and send it to the client
        HospitalInfo response = mapToHospitalResponse(savedHospitalEntity);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    // gRPC service method to update an existing hospital
    @Transactional
    @Override
    public void updateHospital(UpdateHospitalRequest request, StreamObserver<HospitalInfo> responseObserver) {
        try {
            // Find the existing hospital entity by ID
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));
            // Update the fields of the existing hospital entity with the new values
            hospitalEntity.setName(request.getName());
            hospitalEntity.setLocation(request.getLocation());
            hospitalEntity.setNumber_of_beds(request.getNumberOfBeds());
            hospitalEntity.setFounding_date(request.getFoundingDate());

            // Save the updated hospital entity to the database
            HospitalModel updatedHospitalEntity = hospitalRepository.save(hospitalEntity);

            // Map the updated hospital entity to gRPC response and send it to the client
            HospitalInfo response = mapToHospitalResponse(updatedHospitalEntity);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            // Handle the case where the hospital is not found
            log.error("Error updating hospital. {}", ex.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription("Hospital not found").asRuntimeException());
        } catch (Exception ex) {
            log.error("Error updating hospital. {}", ex.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }
    // gRPC service method to delete an existing hospital
    @Transactional
    @Override
    public void deleteHospital(DeleteHospitalRequest request, StreamObserver<HospitalInfo> responseObserver) {
        try {
            // Find the existing hospital entity by ID
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));
            // Delete the hospital entity from the database
            hospitalRepository.delete(hospitalEntity);
            // Map the deleted hospital entity to gRPC response and send it to the client
            HospitalInfo deletedHospitalResponse = mapToHospitalResponse(hospitalEntity);
            responseObserver.onNext(deletedHospitalResponse);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            log.error("Error deleting hospital. {}", ex.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription("Hospital not found").asRuntimeException());
        } catch (Exception ex) {
            log.error("Error deleting hospital. {}", ex.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }
    // Helper method to map a HospitalModel entity to a gRPC response
    private HospitalInfo mapToHospitalResponse(HospitalModel hospitalEntity) {
       return GrpcUtils.mapToHospitalResponse(hospitalEntity);
    }
    // gRPC service method to list all hospitals
    @Transactional
    @Override
    public void listHospitals(ListHospitalsRequest request, StreamObserver<HospitalsList> responseObserver) {
        List<HospitalModel> hospitals = hospitalRepository.findAll();

        List<HospitalInfo> hospitalResponses = hospitals.stream()
                .map(this::mapToHospitalResponse)
                .collect(Collectors.toList());

        HospitalsList hospitalsListResponse = HospitalsList.newBuilder()
                .addAllHospitals(hospitalResponses)
                .build();

        responseObserver.onNext(hospitalsListResponse);
        responseObserver.onCompleted();
    }
    // gRPC service method to list all patients in a hospital
    @Transactional
    @Override
    public void listPatientsInHospital(ListPatientsInHospitalRequest request, StreamObserver<PatientListResponse> responseObserver) {
        try {
            // Find the hospital entity by ID
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getHospitalId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));
            // Get the list of patients associated with the hospital
            List<PatientModel> patientsInHospital = new ArrayList<>(hospitalEntity.getPatients());
            // Map each patient entity to a gRPC response
            List<PatientInfo> patientResponses = patientsInHospital.stream()
                    .map(GrpcUtils::mapToPatientInfo)
                    .collect(Collectors.toList());
            // Build the gRPC response containing the list of patients
            PatientListResponse patientListResponse = PatientListResponse.newBuilder()
                    .addAllPatients(patientResponses)
                    .build();
            responseObserver.onNext(patientListResponse);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            ex.printStackTrace();
            log.error("Error retrieving patients in hospital. {}", ex.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error retrieving patients in hospital. {}", ex.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }
    // gRPC service method to record a visit
    @Override
    public void recordVisit(RecordVisitRequest request, StreamObserver<VisitInfo> responseObserver) {
        try {
            // Find the patient entity by ID
            PatientModel patientEntity = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
            // Find the hospital entity by ID
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getHospitalId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));
            // Create a new VisitModel instance from the gRPC request
            VisitModel visitModel = new VisitModel(
                    request.getPatientId(),
                    request.getHospitalId(),
                    request.getAge(),
                    request.getGender()
            );

            // Save the new visit entity to the database
            VisitModel savedVisit = visitRepository.save(visitModel);

            // Map the saved visit entity to gRPC response and send it to the client
            VisitInfo visitInfoResponse = VisitInfo.newBuilder()
                    .setPatientId(savedVisit.getPatientId())
                    .setHospitalId(savedVisit.getHospitalId())
                    .setAge(savedVisit.getAge())
                    .setGender(savedVisit.getGender())
                    .setVisitDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()))
                    .build();

            responseObserver.onNext(visitInfoResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL.withDescription("Error recording visit").asRuntimeException());
        }
    }

//Director Request
// gRPC service method to compute and save visit aggregates
    @Override
    public void computeAndSaveVisitAggregates(ComputeAndSaveVisitAggregatesRequest request,
                                              StreamObserver<ComputeAndSaveVisitAggregatesResponse> responseObserver) {
        try{

        int hospitalId = request.getHospitalId();
        // Invoke the visit service to compute and save visit aggregates
        visitService.computeAndSaveVisitAggregates(hospitalId);
        // Build and send the success response to the client
        ComputeAndSaveVisitAggregatesResponse response = ComputeAndSaveVisitAggregatesResponse.newBuilder().build();
        log.info("Success: Visit aggregates computed and saved successfully.");
        responseObserver.onNext(response);
        responseObserver.onCompleted();}
        catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void getVisitAggregates(GetVisitAggregatesRequest request, StreamObserver<VisitAggregatesList> responseObserver) {
        try {
            int hospitalId = request.getHospitalId();

            // Fetch visit aggregates from the repository
            List<VisitAggregateModel> visitAggregates = visitAggregateRepository.findByHospitalId(hospitalId);
            log.info("Number of visit aggregates fetched: {}", visitAggregates.size());
            // Convert VisitAggregateModel instances to VisitAggregateInfo
            List<VisitAggregateInfo> visitAggregatesInfo = visitAggregates.stream()
                    .map(this::convertToVisitAggregateInfo)
                    .collect(Collectors.toList());

            // Build the gRPC response
            VisitAggregatesList response = VisitAggregatesList.newBuilder()
                    .addAllVisitAggregates(visitAggregatesInfo)
                    .build();

            // Send the response to the client
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions, log the error, and provide a meaningful response to the client
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL.withDescription("Error fetching visit aggregates").asRuntimeException());
        }
    }


    private VisitAggregateInfo convertToVisitAggregateInfo(VisitAggregateModel visitAggregateModel) {
        return VisitAggregateInfo.newBuilder()
                .setId(visitAggregateModel.getId())
                .setHospitalId(visitAggregateModel.getHospitalId())
                .setVisitYear(visitAggregateModel.getVisitYear())
                .setVisitMonth(visitAggregateModel.getVisitMonth())
                .setAverageAge(visitAggregateModel.getAverageAge())
                .setGender(visitAggregateModel.getGender())
                .build();
    }


}








