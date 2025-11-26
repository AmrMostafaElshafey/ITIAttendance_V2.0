package com.iti.attendance.controller;

import com.iti.attendance.service.AttendanceRecordService;
import com.iti.attendance.service.LeaveRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
public class EmployeePortalController {

    private final AttendanceRecordService attendanceRecordService;
    private final LeaveRequestService leaveRequestService;

    public EmployeePortalController(AttendanceRecordService attendanceRecordService, LeaveRequestService leaveRequestService) {
        this.attendanceRecordService = attendanceRecordService;
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping("/portal")
    public String portal(Model model) {
        model.addAttribute("records", attendanceRecordService.findAllActive());
        model.addAttribute("leaves", leaveRequestService.findAllActive());
        return "employee-portal";
    }
}
