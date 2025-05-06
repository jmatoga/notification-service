package jm.notificationservice.job;

import io.micrometer.core.instrument.Counter;
import jm.notificationservice.configuration.RabbitMQConfig;
import jm.notificationservice.model.EChannel;
import jm.notificationservice.model.EPriority;
import jm.notificationservice.model.EStatus;
import jm.notificationservice.model.Notification;
import jm.notificationservice.service.NotificationService;
import jm.notificationservice.service.QuartzNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationJob implements Job {
    private final RabbitTemplate rabbitTemplate;
    private final NotificationService notificationService;
    private final QuartzNotificationService quartzNotificationService;
    private final Counter notificationSentCounter;
    private final Counter notificationFailedCounter;

    @Override
    public void execute(JobExecutionContext context) {
        UUID notificationId = UUID.fromString(context.getJobDetail().getJobDataMap().getString("notificationId"));

        Notification notification = notificationService.getNotification(notificationId);
        if (notification.getStatus().equals(EStatus.IN_PROGRESS) && notification.getRetryCount() < 3) {
            String queue = notification.getChannel().equals(EChannel.PUSH) ? RabbitMQConfig.PUSH_ROUTING_KEY
                                   : RabbitMQConfig.EMAIL_ROUTING_KEY;

            try {
                Notification finalNotification = notification;
                MessagePostProcessor processor = message -> {
                    message.getMessageProperties().setPriority(finalNotification.getPriority().equals(EPriority.HIGH) ? 100 : 50);
                    return message;
                };
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, queue, notification, processor);
                log.info("SENDING | Notification with ID {} successfully sent to queue.", notificationId);
                notificationSentCounter.increment();
            } catch (DataAccessException e) {
                log.error("SENDING | Failed to send notification with ID {}. Retrying...", notificationId);
                notificationFailedCounter.increment();
            } finally {
                quartzNotificationService.scheduleDelete(notification);
            }

            notification = notificationService.getNotification(notificationId);
            if (notification != null && notification.getStatus().equals(EStatus.IN_PROGRESS)) {
                scheduleRetry(notification);
            }

        } else if (notification.getStatus().equals(EStatus.DELIVERED)) {
            log.info("DELIVERED | Notification with ID {} successfully delivered to queue.", notificationId);
        } else {
            log.warn("FAILED | Notification with ID {} exceeded retry limit.", notificationId);
            notification.setStatus(EStatus.FAILED);
            notificationService.updateNotification(notification);
            notificationFailedCounter.increment();
        }

        if (notification != null && (notification.getStatus().equals(EStatus.DELIVERED) || notification.getStatus().equals(EStatus.FAILED))) {
            quartzNotificationService.scheduleDelete(notification);
        }
    }

    private void scheduleRetry(Notification notification) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setStatus(EStatus.IN_PROGRESS);
        notificationService.updateNotification(notification);

        long retryDelayMillis = 5000; // 5 sec

        try {
            quartzNotificationService.scheduleNotification(notification, retryDelayMillis);
        } catch (SchedulerException e) {
            log.error("Error scheduling retry for notification with ID {}", notification.getId(), e);
        }
    }
}
