import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StudentAPIClient {


    private static final String MY_URL = "http://localhost:8080";

    public static void main(String[] args) throws Exception {


        // HTTP Client erstellen
        HttpClient client = HttpClient.newHttpClient();

        // Request erstellen
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MY_URL + "/books"))
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
