package br.pucrs.verival;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OrangeHrmLeaveE2EPreflightTest {

    private static final String DEFAULT_BASE_URL = "https://opensource-demo.orangehrmlive.com";

    @Test
    void ambienteE2EOrangeHrm_deveEstarConfiguradoEAlcancavel() throws Exception {
        assumeTrue(Boolean.parseBoolean(envOrDefault("ORANGEHRM_E2E_ENABLED", "false")),
                "E2E real desabilitado. Defina ORANGEHRM_E2E_ENABLED=true e as credenciais para executar.");

        assertAll(
                () -> assertRequiredEnv("ORANGEHRM_EMPLOYEE_USERNAME"),
                () -> assertRequiredEnv("ORANGEHRM_EMPLOYEE_PASSWORD"),
                () -> assertRequiredEnv("ORANGEHRM_ADMIN_USERNAME"),
                () -> assertRequiredEnv("ORANGEHRM_ADMIN_PASSWORD"),
                () -> assertRequiredEnv("ORANGEHRM_LEAVE_TYPE"),
                () -> assertRequiredEnv("ORANGEHRM_FROM_DATE"),
                () -> assertRequiredEnv("ORANGEHRM_TO_DATE")
        );

        String baseUrl = envOrDefault("ORANGEHRM_BASE_URL", DEFAULT_BASE_URL);
        URI loginUri = URI.create(trimTrailingSlash(baseUrl) + "/web/index.php/auth/login");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder(loginUri)
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertAll(
                () -> assertTrue(response.statusCode() >= 200 && response.statusCode() < 400,
                        "Login page should be reachable before browser E2E execution"),
                () -> assertTrue(containsAny(response.body(), List.of("OrangeHRM", "Login", "Username")),
                        "Login page should look like OrangeHRM")
        );
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank())
            return defaultValue;
        return value.trim();
    }

    private static void assertRequiredEnv(String name) {
        assertFalse(envOrDefault(name, "").isBlank(), name + " must be configured");
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/"))
            return value.substring(0, value.length() - 1);
        return value;
    }

    private static boolean containsAny(String text, List<String> expectedFragments) {
        return expectedFragments.stream().anyMatch(text::contains);
    }
}
