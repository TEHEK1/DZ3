package com.example.payments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.*;

@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class AccountApiIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void accountFlow() {
        String userId = "99";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-USER-ID", userId);

        // create account
        ResponseEntity<String> r1 = rest.exchange("/accounts", HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // topup
        String body = "{\"amount\":\"20.00\"}";
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> r2 = rest.exchange("/accounts/topup", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // balance
        ResponseEntity<String> r3 = rest.exchange("/accounts/balance", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(r3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r3.getBody()).isEqualTo("20.00");
    }
} 