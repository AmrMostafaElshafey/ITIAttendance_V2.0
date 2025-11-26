package com.iti.attendance.controller;

import com.iti.attendance.model.AttendanceLimit;
import com.iti.attendance.model.Employee;
import com.iti.attendance.model.NotificationType;
import com.iti.attendance.service.AttendanceLimitService;
import com.iti.attendance.service.AttendanceRecordService;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final EmployeeService employeeService;
    private final AttendanceRecordService attendanceRecordService;
    private final NotificationService notificationService;
    private final AttendanceLimitService attendanceLimitService;

    public ManagerDashboardController(EmployeeService employeeService, AttendanceRecordService attendanceRecordService, NotificationService notificationService, AttendanceLimitService attendanceLimitService) {
        this.employeeService = employeeService;
        this.attendanceRecordService = attendanceRecordService;
        this.notificationService = notificationService;
        this.attendanceLimitService = attendanceLimitService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("currentUserId");
        if (managerId == null) {
            return "redirect:/login";
        }

        Optional<Employee> managerOpt = employeeService.findById(managerId);
        if (managerOpt.isEmpty()) {
            return "redirect:/login";
        }

        Employee manager = managerOpt.get();
        List<Employee> team = employeeService.findByManager(manager);
        int limit = attendanceLimitService.getActiveLimit().map(AttendanceLimit::getLimitPercent).orElse(80);

        Map<Long, Double> percentages = new HashMap<>();
        team.forEach(emp -> percentages.put(emp.getId(), attendanceRecordService.calculateAttendancePercentage(emp.getId())));

        model.addAttribute("manager", manager);
        model.addAttribute("team", team);
        model.addAttribute("percentages", percentages);
        model.addAttribute("limit", limit);
        return "manager-dashboard";
    }

    @PostMapping("/message")
    public String sendMessage(@RequestParam Long employeeId, @RequestParam String content, HttpSession session) {
        Long managerId = (Long) session.getAttribute("currentUserId");
        if (managerId == null) {
            return "redirect:/login";
        }
        Optional<Employee> managerOpt = employeeService.findById(managerId);
        Optional<Employee> employeeOpt = employeeService.findById(employeeId);
        if (managerOpt.isPresent() && employeeOpt.isPresent()) {
            notificationService.sendNotification(employeeOpt.get(), managerOpt.get(), "تنبيه نسبة الحضور", content, NotificationType.MANAGER_ALERT);
        }
        return "redirect:/manager/dashboard";
    }
}
