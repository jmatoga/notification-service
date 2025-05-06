package jm.notificationservice.model;

import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {
    private UUID id;
    private String message;
    private EChannel channel;
    private EStatus status;
    private EPriority priority;
    private String recipient;
    private LocalDateTime scheduledTime;
    private ZoneId timezone;
    private int retryCount;
}
