/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class NominatimClient {
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";
    private final HttpClient httpClient; // Singleton instance

    public NominatimClient(ExecutorService serviceTaskExecutor) {
        // Initialize the HttpClient instance here
        httpClient =
                HttpClient.newBuilder()
                        .executor(serviceTaskExecutor)
                        .connectTimeout(Duration.ofSeconds(10))
                        .version(HttpClient.Version.HTTP_2) // or HTTP_1_1 if needed
                        .build();
    }

    public HttpResponse<String> search(String areaName) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(areaName, StandardCharsets.UTF_8);
        String url =
                String.format(
                        "%s?q=%s&format=json&addressdetails=1&limit=1",
                        NOMINATIM_API_URL, encodedQuery);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "FloridaStormsApp/1.0")
                        .GET()
                        .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
