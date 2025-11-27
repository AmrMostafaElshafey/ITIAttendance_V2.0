package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.Organization;
import com.iti.attendance.security.EmployeeUserDetails;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.OrganizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalModelAttributes {

    private final EmployeeService employeeService;
    private final OrganizationService organizationService;

    public GlobalModelAttributes(EmployeeService employeeService, OrganizationService organizationService) {
        this.employeeService = employeeService;
        this.organizationService = organizationService;
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

    @ModelAttribute("activeOrganization")
    public Organization activeOrganization() {
        return organizationService.findAllActive().stream().findFirst().orElse(null);
    }
}
