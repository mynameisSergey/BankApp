package com.notifications.controller;

import com.notifications.dto.NotificationDto;
import com.notifications.service.NotificationsService;
import io.micrometer.tracing.Span;
import lombok.RequiredArgsConstructor;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class NotificationsConsumer {

    private final JwtDecoder jwtDecoder;
    private final NotificationsService notificationsService;
    private final Tracer tracer;
    private final Propagator propagator;

    @KafkaListener(topics = "notification", groupId = "notification-group")
    public void consume(ConsumerRecord<String, NotificationDto> record, @Header("Authorization") String authorizationHeader) {
        Span.Builder extractedSpanBuilder = propagator.extract(record.headers(), (headers, key) -> {
            if (headers.lastHeader(key) != null) {
                return new String(headers.lastHeader(key).value(), StandardCharsets.UTF_8);
            }
            return null;
        });
        Span span = extractedSpanBuilder.name("kafka-consumer").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("kafka.topic", record.topic());
            span.tag("kafka.key", record.key());

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                span.tag("kafka.jwt", "missing");
                return;
            }

            String token = authorizationHeader.substring(7);
            Jwt jwt = jwtDecoder.decode(token);

            List<String> roles = ((Map<String, List<String>>) jwt.getClaim("realm_access")).get("roles");
            if (roles == null || !roles.contains("ROLE_NORIFICATIONS")) {
                span.tag("kafka.jwt", "no_role_norifications");
                return;
            }

            span.event("kafka.processed.success");
            notificationsService.sendNotification(record.value());
        } catch (JwtException e) {
            span.tag("kafka.jwt", "invalid");
            span.tag("kafka.error", e.getMessage());
        } finally {
            span.end();
        }
    }
}
