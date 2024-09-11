package cz.muni.fi.controller;

import cz.muni.fi.storage.Storage;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping("/api/traversal")
public class TraversalController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello(@RequestParam String name, @RequestParam String anotherServiceAddress) {
        if (Objects.equals(name, "test")) {
            try {
                HttpClient client = HttpClient.newHttpClient();

                URI uri = new URIBuilder("http://localhost:8080/api/traversal/hello")
                        .addParameter("name", "Julka")
                        .addParameter("anotherServiceAddress", anotherServiceAddress)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                name = response.body();
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }

            return ResponseEntity.ok("Hello " + name);
        }
        return ResponseEntity.ok(name);
    }
}