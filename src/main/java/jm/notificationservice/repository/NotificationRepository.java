package jm.notificationservice.repository;

import jm.notificationservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, UUID> {
}
