package HIS.HIS_demo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import hospital.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HospitalServiceTest {

    private HospitalServiceGrpc.HospitalServiceBlockingStub blockingStub;
    private ManagedChannel channel;

    @BeforeEach
    public void setUp() {
        // Create a gRPC channel and stub
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        blockingStub = HospitalServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void tearDown() {
        // Shutdown the channel after each test
        channel.shutdown();
    }

    @Test
    public void testCreateHospital() {
        // Create a request
        CreateHospitalRequest request = CreateHospitalRequest.newBuilder()
                .setName("Sample Hospital")
                .setLocation("City Center")
                .setNumberOfBeds(200)
                .setFoundingDate("2022-01-15")
                .build();

        // Call the gRPC method
        HospitalInfo response = blockingStub.createHospital(request);

        assertEquals("Sample Hospital", response.getName());
    }
    @Test
    public void testUpdateHospital() {
        // Create a hospital first
        CreateHospitalRequest createRequest = CreateHospitalRequest.newBuilder()
                .setName("HospitalToUpdate")
                .setLocation("City Center")
                .setNumberOfBeds(150)
                .setFoundingDate("2022-01-15")
                .build();

        // Call the gRPC method to create the hospital
        HospitalInfo createdHospital = blockingStub.createHospital(createRequest);

        // Update the hospital
        UpdateHospitalRequest updateRequest = UpdateHospitalRequest.newBuilder()
                .setId(createdHospital.getId())
                .setName("Updated Hospital")
                .setLocation("New Location")
                .setNumberOfBeds(200)
                .setFoundingDate("2022-02-01")
                .build();

        // Call the gRPC method to update the hospital
        HospitalInfo updatedHospital = blockingStub.updateHospital(updateRequest);

        assertEquals("Updated Hospital", updatedHospital.getName());
        assertEquals("New Location", updatedHospital.getLocation());
        assertEquals(200, updatedHospital.getNumberOfBeds());
    }

    @Test
    public void testDeleteHospital() {
        // Create a hospital first
        CreateHospitalRequest createRequest = CreateHospitalRequest.newBuilder()
                .setName("HospitalToDelete")
                .setLocation("City Center")
                .setNumberOfBeds(100)
                .setFoundingDate("2022-01-15")
                .build();

        // Call the gRPC method to create the hospital
        HospitalInfo createdHospital = blockingStub.createHospital(createRequest);

        // Delete the hospital
        DeleteHospitalRequest deleteRequest = DeleteHospitalRequest.newBuilder()
                .setId(createdHospital.getId())
                .build();

        // Call the gRPC method to delete the hospital
        HospitalInfo deletedHospital = blockingStub.deleteHospital(deleteRequest);

        assertEquals(createdHospital.getId(), deletedHospital.getId());
    }


    @Test
    public void testListHospitals() {
        ListHospitalsRequest request = ListHospitalsRequest.newBuilder().build();

        HospitalsList response = blockingStub.listHospitals(request);

        assertTrue(response.getHospitalsList().size() > 0);

        System.out.println("Received Hospitals List: " + response.getHospitalsList());
    }

}

