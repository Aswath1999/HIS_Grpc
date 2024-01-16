import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import HIS.HIS_demo.service.PatientGrpcServiceImpl;
import org.mockito.ArgumentCaptor;
import HIS.HIS_demo.Repository.HospitalRepository;
import HIS.HIS_demo.Repository.PatientRepository;
import HIS.HIS_demo.entities.HospitalModel;
import HIS.HIS_demo.entities.PatientModel;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PatientGrpcServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcServiceImplTest.class);

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @InjectMocks
    private PatientGrpcServiceImpl patientGrpcService;

    @Test
    void testCreatePatient() {
        // Mock the input request
        CreatePatientRequest request = CreatePatientRequest.newBuilder()
                .setName("John Doe")
                .setSex("Male")
                .setDateOfBirth("1990-01-01T00:00:00Z")
                .setAddress("123 Main St")
                .setPhoneNumber("123-456-7890")
                .build();

        // Mock the saved patient entity
        PatientModel savedPatientEntity = new PatientModel();
        savedPatientEntity.setId(1);
        savedPatientEntity.setName(request.getName());
        savedPatientEntity.setSex(request.getSex());
        savedPatientEntity.setDateOfBirth(Instant.parse(request.getDateOfBirth()));
        savedPatientEntity.setAddress(request.getAddress());
        savedPatientEntity.setPhoneNumber(request.getPhoneNumber());

        // Mock the response observer
        StreamObserver<PatientInfo> responseObserver = mock(StreamObserver.class);


        // Mock the patientRepository.save() method
        when(patientRepository.save(any(PatientModel.class))).thenReturn(savedPatientEntity);

        // Perform the service method invocation
        patientGrpcService.createPatient(request, responseObserver);

        // Verify that the response is sent with the correct data
        PatientInfo expectedResponse = PatientInfo.newBuilder()
                .setId(savedPatientEntity.getId())
                .setName(savedPatientEntity.getName())
                .setSex(savedPatientEntity.getSex())
                .setDateOfBirth(Timestamp.newBuilder().setSeconds(savedPatientEntity.getDateOfBirth().getEpochSecond()))
                .setAddress(savedPatientEntity.getAddress())
                .setPhoneNumber(savedPatientEntity.getPhoneNumber())
                .setAge(savedPatientEntity.getAge())
                .build();

        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
    }
    @Test
    void testUpdatePatient() {
        UpdatePatientRequest request = UpdatePatientRequest.newBuilder()
                .setId(1)
                .setName("Updated Name")
                .setSex("Male")
                .setDateOfBirth("1990-01-01T00:00:00Z")
                .setAddress("Updated Address")
                .setPhoneNumber("123-456-7890")
                .build();

        PatientModel existingPatientEntity = new PatientModel();
        existingPatientEntity.setId(1);
        existingPatientEntity.setName("John Doe");
        existingPatientEntity.setSex("Male");
        existingPatientEntity.setDateOfBirth(Instant.parse("1990-01-01T00:00:00Z"));
        existingPatientEntity.setAddress("123 Main St");
        existingPatientEntity.setPhoneNumber("123-456-7890");

        PatientModel updatedPatientEntity = new PatientModel();
        updatedPatientEntity.setId(1);
        updatedPatientEntity.setName(request.getName());
        updatedPatientEntity.setSex(request.getSex());
        updatedPatientEntity.setDateOfBirth(Instant.parse(request.getDateOfBirth()));
        updatedPatientEntity.setAddress(request.getAddress());
        updatedPatientEntity.setPhoneNumber(request.getPhoneNumber());

        StreamObserver<PatientInfo> responseObserver = mock(StreamObserver.class);

        when(patientRepository.findById(eq(request.getId()))).thenReturn(java.util.Optional.of(existingPatientEntity));
        when(patientRepository.save(any(PatientModel.class))).thenReturn(updatedPatientEntity);

        patientGrpcService.updatePatient(request, responseObserver);

        PatientInfo expectedResponse = PatientInfo.newBuilder()
                .setId(updatedPatientEntity.getId())
                .setName(updatedPatientEntity.getName())
                .setSex(updatedPatientEntity.getSex())
                .setDateOfBirth(Timestamp.newBuilder().setSeconds(updatedPatientEntity.getDateOfBirth().getEpochSecond()))
                .setAddress(updatedPatientEntity.getAddress())
                .setPhoneNumber(updatedPatientEntity.getPhoneNumber())
                .setAge(updatedPatientEntity.getAge())
                .build();

        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
    }

    @Test
    void testDeletePatient() {
        DeletePatientRequest request = DeletePatientRequest.newBuilder()
                .setId(1)
                .build();


        PatientModel existingPatientEntity = new PatientModel();
        existingPatientEntity.setId(1);
        existingPatientEntity.setName("John Doe");
        existingPatientEntity.setSex("Male");
        existingPatientEntity.setDateOfBirth(Instant.parse("1990-01-01T00:00:00Z"));
        existingPatientEntity.setAddress("123 Main St");
        existingPatientEntity.setPhoneNumber("123-456-7890");

        StreamObserver<PatientInfo> responseObserver = mock(StreamObserver.class);

        when(patientRepository.findById(eq(request.getId()))).thenReturn(java.util.Optional.of(existingPatientEntity));

        patientGrpcService.deletePatient(request, responseObserver);

        PatientInfo expectedResponse = PatientInfo.newBuilder()
                .setId(existingPatientEntity.getId())
                .setName(existingPatientEntity.getName())
                .setSex(existingPatientEntity.getSex())
                .setDateOfBirth(Timestamp.newBuilder().setSeconds(existingPatientEntity.getDateOfBirth().getEpochSecond()))
                .setAddress(existingPatientEntity.getAddress())
                .setPhoneNumber(existingPatientEntity.getPhoneNumber())
                .setAge(existingPatientEntity.getAge())
                .build();

        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
    }


    @Test
    void testRegisterPatient() {
        RegisterPatientRequest request = RegisterPatientRequest.newBuilder()
                .setHospitalId(1)
                .setPatientId(2)
                .build();

        HospitalModel hospitalEntity = new HospitalModel();
        hospitalEntity.setId(1);

        PatientModel patientEntity = new PatientModel();
        patientEntity.setId(2);

        StreamObserver<PatientHospitalRegistrationResponse> responseObserver = mock(StreamObserver.class);

        when(hospitalRepository.findById(eq(request.getHospitalId()))).thenReturn(java.util.Optional.of(hospitalEntity));
        when(patientRepository.findById(eq(request.getPatientId()))).thenReturn(java.util.Optional.of(patientEntity));

        when(patientRepository.save(any(PatientModel.class))).thenReturn(patientEntity);
        when(hospitalRepository.save(any(HospitalModel.class))).thenReturn(hospitalEntity);

        patientGrpcService.registerPatient(request, responseObserver);

        PatientHospitalRegistrationResponse expectedResponse = PatientHospitalRegistrationResponse.newBuilder()
                .setPatientId(patientEntity.getId())
                .setHospitalId(hospitalEntity.getId())
                .setDateOfRegistration(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()))
                .build();

        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
    }
    @Test
    public void testListPatients() {
        // Mock data
        List<PatientModel> mockPatients = Arrays.asList(
                new PatientModel("John Doe", "Male", Instant.parse("1990-01-01T00:00:00Z"), "123 Main St", "123-456-7890"),
                new PatientModel("Jane Doe", "Female", Instant.parse("1995-02-15T12:30:00Z"), "456 Oak St", "987-654-3210")
        );

        when(patientRepository.findAll()).thenReturn(mockPatients);

        StreamObserver<PatientListResponse> responseObserver = mock(StreamObserver.class);

        patientGrpcService.listPatients(ListPatientsRequest.getDefaultInstance(), responseObserver);

        ArgumentCaptor<PatientListResponse> responseCaptor = ArgumentCaptor.forClass(PatientListResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PatientListResponse capturedResponse = responseCaptor.getValue();
        assertEquals(2, capturedResponse.getPatientsList().size());
    }

}
