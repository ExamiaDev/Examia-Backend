package com.examia.service.mail;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrevoApiEmailProviderTest {

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String API_KEY = "xkeysib-test-api-key";
    private static final String FROM    = "no-reply@examia.com";

    @Test
    void send_postsExpectedJsonAndHeadersToBrevoEndpoint() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        BrevoApiEmailProvider provider = new BrevoApiEmailProvider(
                builder, API_URL, API_KEY, FROM, "Examia"
        );

        String expectedJson = """
                {
                  "sender":{"email":"no-reply@examia.com","name":"Examia"},
                  "to":[{"email":"alumno@uade.edu.ar"}],
                  "subject":"Examia - Código",
                  "textContent":"Tu código es 123456"
                }
                """;

        server.expect(requestTo(API_URL))
              .andExpect(method(org.springframework.http.HttpMethod.POST))
              .andExpect(header("api-key", API_KEY))
              .andExpect(header("content-type", MediaType.APPLICATION_JSON_VALUE))
              .andExpect(content().json(expectedJson, true))
              .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        provider.send("alumno@uade.edu.ar", "Examia - Código", "Tu código es 123456");

        server.verify();
    }

    @Test
    void send_propagatesExceptionOnHttpError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        BrevoApiEmailProvider provider = new BrevoApiEmailProvider(
                builder, API_URL, API_KEY, FROM, "Examia"
        );

        server.expect(requestTo(API_URL))
              .andRespond(withServerError());

        assertThrows(Exception.class,
                () -> provider.send("alumno@uade.edu.ar", "x", "y"));

        server.verify();
    }

    @Test
    void send_exposesBrevoErrorBodyOn401() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        BrevoApiEmailProvider provider = new BrevoApiEmailProvider(
                builder, API_URL, API_KEY, FROM, "Examia"
        );

        String brevoBody = "{\"code\":\"unauthorized\",\"message\":\"Key not found\"}";
        server.expect(requestTo(API_URL))
              .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(brevoBody));

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> provider.send("alumno@uade.edu.ar", "x", "y"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals(brevoBody, ex.getResponseBodyAsString());

        server.verify();
    }

    @Test
    void constructor_trimsWhitespaceInApiKey() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        // API key con espacios al inicio/final como suele pasar al pegar en Render
        BrevoApiEmailProvider provider = new BrevoApiEmailProvider(
                builder, API_URL, "  " + API_KEY + "\n", FROM, "Examia"
        );

        server.expect(requestTo(API_URL))
              .andExpect(header("api-key", API_KEY)) // se envía sin los espacios
              .andRespond(withSuccess());

        provider.send("alumno@uade.edu.ar", "x", "y");

        server.verify();
    }

    @Test
    void constructor_failsFastWhenApiKeyMissing() {
        RestClient.Builder builder = RestClient.builder();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new BrevoApiEmailProvider(builder, API_URL, "", FROM, "Examia"));
        assertTrue(ex.getMessage().contains("api-key"));
    }

    @Test
    void constructor_failsFastWhenMailFromMissing() {
        RestClient.Builder builder = RestClient.builder();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new BrevoApiEmailProvider(builder, API_URL, API_KEY, "", "Examia"));
        assertTrue(ex.getMessage().contains("mail.from"));
    }
}




