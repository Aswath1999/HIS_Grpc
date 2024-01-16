import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import HIS.HIS_demo.Repository.HospitalRepository;
import HIS.HIS_demo.entities.HospitalModel;
import HIS.HIS_demo.service.HospitalGrpcServiceImpl;
import hospital.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class HospitalGrpcServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcServiceImplTest.class);

    @Mock
    private HospitalRepository hospitalRepository;

    @InjectMocks
    private HospitalGrpcServiceImpl hospitalGrpcService;

    @Test
    void testCreateHospital() {
        // Mock the input request
        CreateHospitalRequest request = CreateHospitalRequest.newBuilder()
                .setName("Hospital A")
                .setLocation("City X")
                .setNumberOfBeds(100)
                .setFoundingDate("2022-01-01T00:00:00Z")
                .build();

        // Mock the saved hospital entity
        HospitalModel savedHospitalEntity = new HospitalModel();
        savedHospitalEntity.setId(1);
        savedHospitalEntity.setName(request.getName());
        savedHospitalEntity.setLocation(request.getLocation());
        savedHospitalEntity.setNumber_of_beds(request.getNumberOfBeds());
        savedHospitalEntity.setFounding_date(request.getFoundingDate());

        // Mock the response observer
        StreamObserver<HospitalInfo> responseObserver = mock(StreamObserver.class);

        // Mock the hospitalRepository.save() method
        when(hospitalRepository.save(any(HospitalModel.class))).thenReturn(savedHospitalEntity);

        // Perform the service method invocation
        hospitalGrpcService.createHospital(request, responseObserver);

        // Verify that the response is sent with the correct data
        HospitalInfo expectedResponse = HospitalInfo.newBuilder()
                .setId(savedHospitalEntity.getId())
                .setName(savedHospitalEntity.getName())
                .setLocation(savedHospitalEntity.getLocation())
                .setNumberOfBeds(savedHospitalEntity.getNumber_of_beds())
                .setFoundingDate(savedHospitalEntity.getFounding_date())
                .build();

        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
    }

    @Test
    void testUpdateHospital() {
        // Mock the input request
        UpdateHospitalRequest request = UpdateHospitalRequest.newBuilder()
                .setId(1)
                .setName("Updated Hospital A")
                .setLocation("Updated City X")
                .setNumberOfBeds(120)
                .setFoundingDate("2022-01-01T00:00:00Z")
                .build();

        // Mock the existing hospital entity
        HospitalModel existingHospitalEntity = new HospitalModel();
        existingHospitalEntity.setId(1);
        existingHospitalEntity.setName("Hospital A");
        existingHospitalEntity.setLocation("City X");
        existingHospitalEntity.setNumber_of_beds(100);
        existingHospitalEntity.setFounding_date("2022-01-01T00:00:00Z");

        // Mock the updated hospital entity
        HospitalModel updatedHospitalEntity = new HospitalModel();
        updatedHospitalEntity.setId(1);
        updatedHospitalEntity.setName(request.getName());
        updatedHospitalEntity.setLocation(request.getLocation());
        updatedHospitalEntity.setNumber_of_beds(request.getNumberOfBeds());
        updatedHospitalEntity.setFounding_date(request.getFoundingDate());

        // Mock the response observer
        StreamObserver<HospitalInfo> responseObserver = mock(StreamObserver.class);

        // Mock the hospitalRepository.findById() and hospitalRepository.save() methods
        when(hospitalRepository.findById(eq(request.getId()))).thenReturn(java.util.Optional.of(existingHospitalEntity));
        when(hospitalRepository.save(any(HospitalModel.class))).thenReturn(updatedHospitalEntity);

        // Perform the service method invocation
        hospitalGrpcService.updateHospital(request, responseObserver);

        // Verify that the response is sent with the correct data
        HospitalInfo expectedResponse = HospitalInfo.newBuilder()
                .setId(updatedHospitalEntity.getId())
                .setName(updatedHospitalEntity.getName())
                .setLocation(updatedHospitalEntity.getLocation())
                .setNumberOfBeds(updatedHospitalEntity.getNumber_of_beds())
                .setFoundingDate(updatedHospitalEntity.getFounding_date())
                .build();

        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
    }



    @Test
    void testListHospitals() {
        // Mock data
        List<HospitalModel> mockHospitals = Arrays.asList(
                new HospitalModel("Hospital A", "City X", 100, "2022-01-01T00:00:00Z"),
                new HospitalModel("Hospital B", "City Y", 150, "2022-02-01T00:00:00Z")
        );

        when(hospitalRepository.findAll()).thenReturn(mockHospitals);
        StreamObserver<HospitalsList> responseObserver=mock(StreamObserver.class);
        // Invoke the service method
        hospitalGrpcService.listHospitals(ListHospitalsRequest.getDefaultInstance(), responseObserver);

        // Verify that the response observer receives the expected response
        ArgumentCaptor<HospitalsList> responseCaptor = ArgumentCaptor.forClass(HospitalsList.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        // Validate the content of the response
        HospitalsList capturedResponse = responseCaptor.getValue();
        assertEquals(2, capturedResponse.getHospitalsList().size()); // Assuming 2 hospitals in the mock data
        // Additional validation if needed for the content of the response
    }
}
