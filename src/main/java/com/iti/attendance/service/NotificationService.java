package com.iti.attendance.service;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.Notification;
import com.iti.attendance.model.NotificationType;
import com.iti.attendance.model.Role;
import com.iti.attendance.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeService employeeService;

    public NotificationService(NotificationRepository notificationRepository, EmployeeService employeeService) {
        this.notificationRepository = notificationRepository;
        this.employeeService = employeeService;
    }

    public Notification sendNotification(Employee recipient, Employee sender, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForRecipient(Employee recipient) {
        return notificationRepository.findByRecipientAndDeletedFalseOrderByCreatedAtDesc(recipient);
    }

    public List<Notification> notifyHrForPendingEmployee(Employee pendingEmployee) {
        List<Role> hrRoles = List.of(Role.HR_MANAGER, Role.HR_EMPLOYEE);
        List<Notification> created = new ArrayList<>();
        employeeService.findByRoles(hrRoles).forEach(hr -> {
            Notification n = new Notification();
            n.setRecipient(hr);
            n.setSender(pendingEmployee);
            n.setTitle("مراجعة حساب جديد");
            n.setMessage("يرجى مراجعة بيانات الموظف: " + pendingEmployee.getName());
            n.setType(NotificationType.PENDING_APPROVAL);
            created.add(notificationRepository.save(n));
        });
        return created;
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setReadFlag(true);
            notificationRepository.save(notification);
        });
    }
}
