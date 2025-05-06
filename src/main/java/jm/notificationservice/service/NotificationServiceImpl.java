package jm.notificationservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import jakarta.transaction.Transactional;
import jm.notificationservice.dto.NotificationDTO;
import jm.notificationservice.exception.NotificationNotFoundException;
import jm.notificationservice.model.EStatus;
import jm.notificationservice.model.Notification;
import jm.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static io.micrometer.core.instrument.Timer.start;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final QuartzNotificationService quartzNotificationService;
    private final Counter notificationCreatedCounter;
    private final Timer notificationProcessingTimer;

    @Override
    @Transactional
    public UUID scheduleNotification(NotificationDTO notificationDTO) {
        Sample sample = start();

        Notification notification = Notification.builder()
                        .id(UUID.randomUUID())
                        .message(notificationDTO.getMessage())
                        .channel(notificationDTO.getChannel())
                        .priority(notificationDTO.getPriority())
                        .recipient(notificationDTO.getRecipient())
                        .scheduledTime(notificationDTO.getScheduledTime())
                        .timezone(notificationDTO.getTimezone())
                        .status(EStatus.IN_PROGRESS)
                        .retryCount(0)
                        .build();

        notificationRepository.save(notification);
        notificationCreatedCounter.increment();
        sample.stop(notificationProcessingTimer);

        return notification.getId();
    }

    @Override
    public Notification getNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }

    @Override
    @Transactional
    public boolean forceSendNotification(UUID notificationId) throws SchedulerException {
        Notification notification = getNotification(notificationId);

        if (notification != null && notification.getStatus() == EStatus.IN_PROGRESS) {
            notification.setScheduledTime(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
            notificationRepository.save(notification);
            quartzNotificationService.scheduleNotification(notification, 0);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) throws SchedulerException {
        notificationRepository.deleteById(notificationId);
        quartzNotificationService.cancelNotification(notificationId);
    }

    @Override
    public void updateNotification(Notification notification) {
        notificationRepository.save(notification);
    }
}
