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
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final VisitAggregateRepository visitAggregateRepository;
    @Autowired
    private final VisitService visitService;
    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcServiceImpl.class);
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
            hospitalEntity.setName(request.getName());
            hospitalEntity.setLocation(request.getLocation());
            hospitalEntity.setNumber_of_beds(request.getNumberOfBeds());
            hospitalEntity.setFounding_date(request.getFoundingDate());

            HospitalModel updatedHospitalEntity = hospitalRepository.save(hospitalEntity);

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
            ex.printStackTrace();
            log.error("Error retrieving patients in hospital. {}", ex.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error retrieving patients in hospital. {}", ex.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Override
    public void recordVisit(RecordVisitRequest request, StreamObserver<VisitInfo> responseObserver) {
        try {
            PatientModel patientEntity = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

            HospitalModel hospitalEntity = hospitalRepository.findById(request.getHospitalId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));
            VisitModel visitModel = new VisitModel(
                    request.getPatientId(),
                    request.getHospitalId(),
                    request.getAge(),
                    request.getGender()
            );

            VisitModel savedVisit = visitRepository.save(visitModel);

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

    @Override
    public void computeAndSaveVisitAggregates(ComputeAndSaveVisitAggregatesRequest request,
                                              StreamObserver<ComputeAndSaveVisitAggregatesResponse> responseObserver) {
        try{
        int hospitalId = request.getHospitalId();
        visitService.computeAndSaveVisitAggregates(hospitalId);
        ComputeAndSaveVisitAggregatesResponse response = ComputeAndSaveVisitAggregatesResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();}
        catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void getVisitAggregates(GetVisitAggregatesRequest request, StreamObserver<VisitAggregatesList> responseObserver) {
        int hospitalId = request.getHospitalId();


        List<VisitAggregateModel> visitAggregates = visitAggregateRepository.findByHospitalId(hospitalId);

        List<VisitAggregateInfo> visitAggregatesInfo = visitAggregates.stream()
                .map(this::convertToVisitAggregateInfo)
                .collect(Collectors.toList());


        VisitAggregatesList response = VisitAggregatesList.newBuilder()
                .addAllVisitAggregates(visitAggregatesInfo)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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








