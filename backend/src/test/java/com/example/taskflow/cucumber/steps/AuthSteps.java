package com.example.taskflow.cucumber.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    /**
     * Never throws on 4xx/5xx — every response comes back as a ResponseEntity
     * so we can assert on error status codes directly in Then steps.
     */
    private final RestClient restClient = RestClient.builder()
            .defaultStatusHandler(status -> true, (req, res) -> { })
            .build();

    @LocalServerPort
    private int port;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private ResponseEntity<String> lastResponse;
    private String authToken;
    private String lastCreatedId;

    // ── Given ────────────────────────────────────────────────────────────────

    @Given("a user is registered with email {string} and password {string}")
    public void registerUser(String email, String password) {
        String body = """
                {"name": "Test User", "email": "%s", "password": "%s"}
                """.formatted(email, password);
        restClient.post()
                .uri(url("/auth/register"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @And("I log in with email {string} and password {string}")
    public void loginAndStoreToken(String email, String password) {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
        ResponseEntity<Map> response = restClient.post()
                .uri(url("/auth/login"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(Map.class);
        authToken = (String) response.getBody().get("access_token");
    }

    @And("I create a project named {string}")
    public void createProject(String name) {
        String body = """
                {"name": "%s"}
                """.formatted(name);
        ResponseEntity<Map> response = restClient.post()
                .uri(url("/projects"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(Map.class);
        lastCreatedId = (String) response.getBody().get("id");
    }

    @When("I GET the previously created project with the obtained token")
    public void getLastCreatedProjectWithToken() {
        lastResponse = restClient.get()
                .uri(url("/projects/" + lastCreatedId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .toEntity(String.class);
    }

    // ── When — POST ───────────────────────────────────────────────────────────

    @When("I POST to {string} with body:")
    public void sendPost(String path, String json) {
        lastResponse = restClient.post()
                .uri(url(path))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    // ── When — GET variants ───────────────────────────────────────────────────

    @When("I GET {string} without a token")
    public void getWithoutToken(String path) {
        lastResponse = restClient.get()
                .uri(url(path))
                .retrieve()
                .toEntity(String.class);
    }

    @When("I GET {string} with the obtained token")
    public void getWithObtainedToken(String path) {
        lastResponse = restClient.get()
                .uri(url(path))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .toEntity(String.class);
    }

    @When("I GET {string} with an expired JWT")
    public void getWithExpiredJwt(String path) {
        lastResponse = restClient.get()
                .uri(url(path))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildJwt(jwtSecret, -10_000L))
                .retrieve()
                .toEntity(String.class);
    }

    @When("I GET {string} with a JWT signed by a wrong secret")
    public void getWithWrongSecretJwt(String path) {
        String wrongSecret = "wrong-secret-used-for-signing-this-jwt-min-32-chars!";
        lastResponse = restClient.get()
                .uri(url(path))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildJwt(wrongSecret, 86_400_000L))
                .retrieve()
                .toEntity(String.class);
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the response status should be {int}")
    public void checkStatus(int expected) {
        assertThat(lastResponse.getStatusCode().value())
                .as("HTTP status")
                .isEqualTo(expected);
    }

    @Then("the response should contain error {string}")
    public void checkErrorContains(String expected) {
        assertThat(lastResponse.getBody())
                .as("response body")
                .contains(expected);
    }

    @Then("the response error field should be {string}")
    public void checkErrorField(String expected) {
        assertThat(lastResponse.getBody())
                .as("response body error field")
                .contains("\"error\":\"" + expected + "\"");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a JWT signed with the given secret.
     * A negative {@code expiresInMs} produces an already-expired token.
     */
    private String buildJwt(String secret, long expiresInMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiresInMs))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
