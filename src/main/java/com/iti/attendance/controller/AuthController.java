package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.EmployeeStatus;
import com.iti.attendance.model.Role;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.NotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final EmployeeService employeeService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(EmployeeService employeeService, NotificationService notificationService, PasswordEncoder passwordEncoder) {
        this.employeeService = employeeService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Employee employee, Model model) {
        employee.setStatus(EmployeeStatus.PENDING);
        if (employee.getPassword() != null) {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }
        try {
            employeeService.save(employee);
            model.addAttribute("message", "تم استلام التسجيل وسيتم مراجعته بواسطة الموارد البشرية");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @GetMapping("/login")
    public String employeeLoginPage() {
        return "login";
    }

    @GetMapping("/admin/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/admin/pending/notify")
    public String notifyHr(@RequestParam Long id, Model model) {
        employeeService.findById(id).ifPresent(employee -> model.addAttribute("employee", employee));
        model.addAttribute("message", "تم إرسال إشعار للموارد البشرية للموافقة على بياناتك");
        return "admin-pending";
    }

    @GetMapping("/pending")
    public String pending(Model model, @SessionAttribute(name = "SPRING_SECURITY_LAST_EXCEPTION", required = false) Exception lastException) {
        if (lastException instanceof org.springframework.security.authentication.DisabledException) {
            model.addAttribute("message", lastException.getMessage());
        }
        return "admin-pending";
    }
}
