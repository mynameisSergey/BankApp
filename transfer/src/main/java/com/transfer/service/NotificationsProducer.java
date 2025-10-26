package com.transfer.service;

import com.transfer.configuration.OAuth2TokenProvider;
import com.transfer.dto.NotificationDto;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Service
public class NotificationsProducer {

    private final OAuth2TokenProvider tokenProvider;
    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;
    private final Tracer tracer;
    private final Propagator propagator;

    public void notificate(NotificationDto notificationDto) {
        String token = tokenProvider.getAccessToken();

        System.out.println("token=" + token);

        ProducerRecord<String, NotificationDto> record =
                new ProducerRecord<>("notification", notificationDto.getLogin(), notificationDto);
        record.headers().add("Authorization", ("Bearer " + token).getBytes(StandardCharsets.UTF_8));

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            propagator.inject(currentSpan.context(), record.headers(), (headers, key, value) -> {
                headers.add(key, value.getBytes(StandardCharsets.UTF_8));
            });
        }
        kafkaTemplate.send(record);
    }

}
