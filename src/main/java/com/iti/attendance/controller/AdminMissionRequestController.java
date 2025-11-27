package com.iti.attendance.controller;

import com.iti.attendance.model.LeaveRequest;
import com.iti.attendance.model.RequestType;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.LeaveRequestService;
import com.iti.attendance.service.LeaveTypeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping({"/admin/missions", "/employee/missions"})
public class AdminMissionRequestController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeService employeeService;
    private final LeaveTypeService leaveTypeService;

    public AdminMissionRequestController(LeaveRequestService leaveRequestService, EmployeeService employeeService, LeaveTypeService leaveTypeService) {
        this.leaveRequestService = leaveRequestService;
        this.employeeService = employeeService;
        this.leaveTypeService = leaveTypeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("leaves", leaveRequestService.findAllActiveByType(RequestType.MISSION));
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("leaveTypes", leaveTypeService.findActiveByType(RequestType.MISSION));
        model.addAttribute("basePath", resolveBasePath());
        model.addAttribute("pageTitle", "المأموريات");
        model.addAttribute("formTitle", "بيانات طلب مأمورية");
        return "admin-missions";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        LeaveRequest request = new LeaveRequest();
        request.setType(RequestType.MISSION);
        model.addAttribute("leaveRequest", request);
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("leaveTypes", leaveTypeService.findActiveByType(RequestType.MISSION));
        model.addAttribute("basePath", resolveBasePath());
        model.addAttribute("formTitle", "بيانات طلب مأمورية");
        return "admin-mission-form";
    }

    @PostMapping
    public String save(@ModelAttribute LeaveRequest leaveRequest,
                       @RequestParam("start") String start,
                       @RequestParam("end") String end,
                       @RequestParam("leaveTypeId") Long leaveTypeId) {
        leaveRequest.setStartDate(LocalDate.parse(start));
        leaveRequest.setEndDate(LocalDate.parse(end));
        leaveTypeService.findById(leaveTypeId).ifPresent(lt -> {
            leaveRequest.setLeaveType(lt);
            leaveRequest.setType(RequestType.MISSION);
        });
        if (!leaveRequestService.isWithinGracePeriod(leaveRequest)) {
            return "redirect:" + resolveBasePath() + "?graceError=true";
        }
        leaveRequestService.save(leaveRequest);
        return "redirect:" + resolveBasePath();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        leaveRequestService.findById(id).ifPresent(lr -> model.addAttribute("leaveRequest", lr));
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("leaveTypes", leaveTypeService.findActiveByType(RequestType.MISSION));
        model.addAttribute("basePath", resolveBasePath());
        model.addAttribute("formTitle", "بيانات طلب مأمورية");
        return "admin-mission-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        leaveRequestService.softDelete(id);
        return "redirect:" + resolveBasePath();
    }

    private String resolveBasePath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean adminContext = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("ROLE_ADMIN")
                            || auth.equals("ROLE_HR_MANAGER")
                            || auth.equals("ROLE_HR_EMPLOYEE"));
            if (adminContext) {
                return "/admin/missions";
            }
        }
        return "/employee/missions";
    }
}
