package jm.notificationservice.dto;

import jm.notificationservice.model.EChannel;
import jm.notificationservice.model.EPriority;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
public class NotificationDTO {
    private String message;
    private EChannel channel;
    private EPriority priority;
    private String recipient;
    private ZoneId timezone;
    private LocalDateTime scheduledTime;
}
