import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StudentAPIClient {

    private static final String MOCK_URL =
            "https://d26c480a-f6d5-4e64-819e-ea0df80bcff0.mock.pstmn.io";

    public static void main(String[] args) throws Exception {


        int[] zahlen = new int[]{80, 90, 70};
        int[] zahlen2 = {80, 90, 70};


        // HTTP Client erstellen
        HttpClient client = HttpClient.newHttpClient();

        // Request erstellen
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOCK_URL + "/api/students"))
                .GET()
                .build();

        // Request senden
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        // Antwort ausgeben
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());



    }
}
