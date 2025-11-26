package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.*;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final EmployeeService employeeService;
    private final NotificationService notificationService;

    public AuthController(EmployeeService employeeService, NotificationService notificationService) {
        this.employeeService = employeeService;
        this.notificationService = notificationService;
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
        employeeService.save(employee);
        model.addAttribute("message", "تم استلام التسجيل وسيتم مراجعته بواسطة الموارد البشرية");
        return "register";
    }

    @GetMapping("/login")
    public String employeeLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String employeeLogin(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        return handleLogin(email, password, model, session);
    }

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        return handleLogin(email, password, model, session);
    }

    private String handleLogin(String email, String password, Model model, HttpSession session) {
        Optional<Employee> employeeOptional = employeeService.findByEmail(email);
        if (employeeOptional.isEmpty() || !employeeOptional.get().getPassword().equals(password)) {
            model.addAttribute("error", "بيانات الدخول غير صحيحة");
            return "login";
        }
        Employee employee = employeeOptional.get();
        if (employee.getStatus() == EmployeeStatus.PENDING) {
            model.addAttribute("employee", employee);
            notificationService.notifyHrForPendingEmployee(employee);
            return "admin-pending";
        }
        session.setAttribute("currentUserId", employee.getId());
        session.setAttribute("currentUserRole", employee.getRole().name());
        if (employee.getRole() == Role.ADMIN || employee.getRole() == Role.HR_MANAGER || employee.getRole() == Role.HR_EMPLOYEE) {
            return "redirect:/admin/dashboard";
        }
        if (employee.getRole() == Role.MANAGER || employee.getRole() == Role.BRANCH_MANAGER) {
            return "redirect:/manager/dashboard";
        }
        return "redirect:/employee/portal";
    }

    @PostMapping("/admin/pending/notify")
    public String notifyHr(@RequestParam Long id, Model model) {
        employeeService.findById(id).ifPresent(employee -> model.addAttribute("employee", employee));
        model.addAttribute("message", "تم إرسال إشعار للموارد البشرية للموافقة على بياناتك");
        return "admin-pending";
    }
}
