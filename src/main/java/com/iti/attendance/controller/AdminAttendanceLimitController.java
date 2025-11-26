package com.iti.attendance.controller;

import com.iti.attendance.model.AttendanceLimit;
import com.iti.attendance.service.AttendanceLimitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/attendance-limit")
public class AdminAttendanceLimitController {

    private final AttendanceLimitService attendanceLimitService;

    public AdminAttendanceLimitController(AttendanceLimitService attendanceLimitService) {
        this.attendanceLimitService = attendanceLimitService;
    }

    @GetMapping
    public String view(Model model) {
        AttendanceLimit limit = attendanceLimitService.getActiveLimit().orElse(new AttendanceLimit());
        model.addAttribute("limit", limit);
        return "admin-attendance-limit";
    }

    @PostMapping
    public String save(@ModelAttribute("limit") AttendanceLimit limit) {
        attendanceLimitService.save(limit);
        return "redirect:/admin/attendance-limit";
    }
}
