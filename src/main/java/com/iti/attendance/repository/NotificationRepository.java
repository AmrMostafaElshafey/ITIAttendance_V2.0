package com.iti.attendance.repository;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.Notification;
import com.iti.attendance.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientAndDeletedFalseOrderByCreatedAtDesc(Employee recipient);
    List<Notification> findByRecipientAndTypeAndDeletedFalseOrderByCreatedAtDesc(Employee recipient, NotificationType type);
}
