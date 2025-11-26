package com.iti.attendance.security;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.EmployeeStatus;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.NotificationService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeService employeeService;
    private final NotificationService notificationService;

    public EmployeeUserDetailsService(EmployeeService employeeService, NotificationService notificationService) {
        this.employeeService = employeeService;
        this.notificationService = notificationService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeService.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("المستخدم غير موجود"));

        if (employee.getStatus() == EmployeeStatus.PENDING) {
            notificationService.notifyHrForPendingEmployee(employee);
            throw new DisabledException("حسابك قيد المراجعة بواسطة الموارد البشرية");
        }

        if (employee.getStatus() == EmployeeStatus.INACTIVE) {
            throw new DisabledException("تم إيقاف الحساب، يرجى التواصل مع الموارد البشرية");
        }

        return new EmployeeUserDetails(employee);
    }
}
