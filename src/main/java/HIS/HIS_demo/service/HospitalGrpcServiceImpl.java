package HIS.HIS_demo.service;
import HIS.HIS_demo.Repository.HospitalRepository;
import HIS.HIS_demo.entities.HospitalModel;
import HIS.HIS_demo.entities.PatientModel;
import HIS.HIS_demo.service.GrpcUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class HospitalGrpcServiceImpl extends HospitalServiceGrpc.HospitalServiceImplBase{
    private final HospitalRepository hospitalRepository;
    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcServiceImpl.class);
    @Autowired
    public HospitalGrpcServiceImpl(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }
    @Transactional
    @Override
    public void createHospital(CreateHospitalRequest request, StreamObserver<HospitalInfo> responseObserver) {
        HospitalModel hospitalEntity = new HospitalModel(
                request.getName(),
                request.getLocation(),
                request.getNumberOfBeds(),
                request.getFoundingDate()
        );

        HospitalModel savedHospitalEntity = hospitalRepository.save(hospitalEntity);

        log.info("Hospital created - ID: {}, Name: {}", savedHospitalEntity.getId(), savedHospitalEntity.getName());

        HospitalInfo response = mapToHospitalResponse(savedHospitalEntity);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    @Transactional
    @Override
    public void updateHospital(UpdateHospitalRequest request, StreamObserver<HospitalInfo> responseObserver) {
        try {
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));

            // Update hospital details
            hospitalEntity.setName(request.getName());
            hospitalEntity.setLocation(request.getLocation());
            hospitalEntity.setNumber_of_beds(request.getNumberOfBeds());
            hospitalEntity.setFounding_date(request.getFoundingDate());

            // Save the updated hospital
            HospitalModel updatedHospitalEntity = hospitalRepository.save(hospitalEntity);

            // Build and send the updated hospital response
            HospitalInfo response = mapToHospitalResponse(updatedHospitalEntity);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            log.error("Error updating hospital. {}", ex.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription("Hospital not found").asRuntimeException());
        } catch (Exception ex) {
            log.error("Error updating hospital. {}", ex.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Transactional
    @Override
    public void deleteHospital(DeleteHospitalRequest request, StreamObserver<HospitalInfo> responseObserver) {
        try {
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));

            // Delete the hospital
            hospitalRepository.delete(hospitalEntity);

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

    private HospitalInfo mapToHospitalResponse(HospitalModel hospitalEntity) {
       return GrpcUtils.mapToHospitalResponse(hospitalEntity);
    }
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

    @Transactional
    @Override
    public void listPatientsInHospital(ListPatientsInHospitalRequest request, StreamObserver<PatientListResponse> responseObserver) {
        try {
            HospitalModel hospitalEntity = hospitalRepository.findById(request.getHospitalId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));

            List<PatientModel> patientsInHospital = new ArrayList<>(hospitalEntity.getPatients());

            List<PatientInfo> patientResponses = patientsInHospital.stream()
                    .map(GrpcUtils::mapToPatientInfo)
                    .collect(Collectors.toList());

            PatientListResponse patientListResponse = PatientListResponse.newBuilder()
                    .addAllPatients(patientResponses)
                    .build();

            responseObserver.onNext(patientListResponse);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            log.error("Error retrieving patients in hospital. {}", ex.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            log.error("Error retrieving patients in hospital. {}", ex.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }





}
