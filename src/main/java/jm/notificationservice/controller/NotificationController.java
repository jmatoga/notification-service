package jm.notificationservice.controller;

import jm.notificationservice.dto.NotificationDTO;
import jm.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<String> createNotification(@RequestBody NotificationDTO notificationDTO) {
        UUID scheduledId = notificationService.scheduleNotification(notificationDTO);
        return ResponseEntity.ok("Notification with id: " + scheduledId.toString() + " scheduled.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getNotification(@PathVariable UUID id) {
        return ResponseEntity.ok("Notification scheduled: " + notificationService.getNotification(id));
    }

    @PostMapping("/{id}/force-send")
    public ResponseEntity<String> forceSendNotification(@PathVariable UUID id) throws SchedulerException {
        if (notificationService.forceSendNotification(id)) {
            return ResponseEntity.ok("Notification scheduled to force send");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification with ID: " + id + " not found.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable UUID id) throws SchedulerException {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification with ID: " + id + " deleted.");
    }
}
