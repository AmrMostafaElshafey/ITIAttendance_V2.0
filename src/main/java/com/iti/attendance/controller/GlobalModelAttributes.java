package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.security.EmployeeUserDetails;
import com.iti.attendance.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalModelAttributes {

    private final EmployeeService employeeService;

    public GlobalModelAttributes(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @ModelAttribute("currentEmployee")
    public Employee populateCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof EmployeeUserDetails userDetails) {
            String email = userDetails.getUsername();
            Optional<Employee> employeeOpt = employeeService.findByEmail(email);
            return employeeOpt.orElse(null);
        }
        return null;
    }
}
