package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final NotificationService notificationService;
    private final EmployeeService employeeService;

    public AdminDashboardController(NotificationService notificationService, EmployeeService employeeService) {
        this.notificationService = notificationService;
        this.employeeService = employeeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("currentUserId");
        if (userId != null) {
            Optional<Employee> employee = employeeService.findById(userId);
            employee.ifPresent(value -> model.addAttribute("notifications", notificationService.getNotificationsForRecipient(value)));
        }
        return "admin-dashboard";
    }
}
