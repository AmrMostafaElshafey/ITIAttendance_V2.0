package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.EmployeeStatus;
import com.iti.attendance.model.Role;
import com.iti.attendance.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final EmployeeService employeeService;

    public AuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
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

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model) {
        Optional<Employee> employeeOptional = employeeService.findByEmail(email);
        if (employeeOptional.isEmpty() || !employeeOptional.get().getPassword().equals(password)) {
            model.addAttribute("error", "بيانات الدخول غير صحيحة");
            return "admin-login";
        }
        Employee employee = employeeOptional.get();
        if (employee.getStatus() == EmployeeStatus.PENDING) {
            model.addAttribute("employee", employee);
            return "admin-pending";
        }
        if (employee.getRole() == Role.ADMIN || employee.getRole() == Role.MANAGER) {
            return "redirect:/admin/dashboard";
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
