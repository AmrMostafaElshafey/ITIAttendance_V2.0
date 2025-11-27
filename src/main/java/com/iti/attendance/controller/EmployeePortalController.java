package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.RequestType;
import com.iti.attendance.service.AttendanceRecordService;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.LeaveRequestService;
import com.iti.attendance.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
public class EmployeePortalController {

    private final AttendanceRecordService attendanceRecordService;
    private final LeaveRequestService leaveRequestService;
    private final NotificationService notificationService;
    private final EmployeeService employeeService;

    public EmployeePortalController(AttendanceRecordService attendanceRecordService, LeaveRequestService leaveRequestService, NotificationService notificationService, EmployeeService employeeService) {
        this.attendanceRecordService = attendanceRecordService;
        this.leaveRequestService = leaveRequestService;
        this.notificationService = notificationService;
        this.employeeService = employeeService;
    }

    @GetMapping("/portal")
    public String portal(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        if (userId == null) {
            return "redirect:/login";
        }
        Employee employee = employeeService.findById(userId).orElse(null);
        if (employee == null) {
            return "redirect:/login";
        }
        model.addAttribute("records", attendanceRecordService.findByEmployee(userId));
        model.addAttribute("leaves", leaveRequestService.findActiveByEmployeeAndType(userId, RequestType.LEAVE));
        model.addAttribute("missions", leaveRequestService.findActiveByEmployeeAndType(userId, RequestType.MISSION));
        model.addAttribute("permits", leaveRequestService.findActiveByEmployeeAndType(userId, RequestType.PERMIT));
        model.addAttribute("notifications", notificationService.getNotificationsForRecipient(employee));
        model.addAttribute("attendancePercent", attendanceRecordService.calculateAttendancePercentage(userId));
        model.addAttribute("employee", employee);
        return "employee-portal";
    }
}
