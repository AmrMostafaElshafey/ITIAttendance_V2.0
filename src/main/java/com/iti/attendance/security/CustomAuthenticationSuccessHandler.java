package com.iti.attendance.security;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        EmployeeUserDetails principal = (EmployeeUserDetails) authentication.getPrincipal();
        Employee employee = principal.getEmployee();

        if (employee.getRole() == Role.ADMIN || employee.getRole() == Role.HR_MANAGER || employee.getRole() == Role.HR_EMPLOYEE) {
            response.sendRedirect("/admin/dashboard");
            return;
        }
        if (employee.getRole() == Role.MANAGER || employee.getRole() == Role.BRANCH_MANAGER || employee.getRole() == Role.TRAINING_MANAGER) {
            response.sendRedirect("/manager/dashboard");
            return;
        }
        response.sendRedirect("/employee/portal");
    }
}
