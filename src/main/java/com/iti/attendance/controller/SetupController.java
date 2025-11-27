package com.iti.attendance.controller;

import com.iti.attendance.model.*;
import com.iti.attendance.service.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/setup")
@SessionAttributes("setupContext")
public class SetupController {

    private final OrganizationService organizationService;
    private final BranchService branchService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final LeaveTypeService leaveTypeService;
    private final AttendanceRuleService attendanceRuleService;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public SetupController(OrganizationService organizationService,
                           BranchService branchService,
                           DepartmentService departmentService,
                           EmployeeService employeeService,
                           LeaveTypeService leaveTypeService,
                           AttendanceRuleService attendanceRuleService,
                           FileStorageService fileStorageService,
                           PasswordEncoder passwordEncoder) {
        this.organizationService = organizationService;
        this.branchService = branchService;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
        this.leaveTypeService = leaveTypeService;
        this.attendanceRuleService = attendanceRuleService;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @ModelAttribute("setupContext")
    public SetupContext setupContext() {
        return new SetupContext();
    }

    @GetMapping
    public String start(@ModelAttribute("setupContext") SetupContext setupContext) {
        if (!setupContext.hasOrganization()) {
            organizationService.findAllActive().stream().findFirst()
                    .ifPresent(org -> setupContext.setOrganizationId(org.getId()));
        }
        return "redirect:/setup/organization";
    }

    @GetMapping("/organization")
    public String organizationStep(Model model, @ModelAttribute("setupContext") SetupContext setupContext) {
        Organization organization = setupContext.hasOrganization()
                ? organizationService.findById(setupContext.getOrganizationId()).orElse(new Organization())
                : new Organization();
        model.addAttribute("organization", organization);
        model.addAttribute("currentStep", 1);
        return "setup-organization";
    }

    @PostMapping("/organization")
    public String saveOrganization(@ModelAttribute Organization organization,
                                   @RequestParam(value = "logo", required = false) MultipartFile logo,
                                   @ModelAttribute("setupContext") SetupContext setupContext) throws IOException {
        String logoPath = fileStorageService.storeFile(logo);
        if (logoPath != null) {
            organization.setLogoPath(logoPath);
        } else if (organization.getId() != null) {
            organizationService.findById(organization.getId())
                    .map(Organization::getLogoPath)
                    .ifPresent(organization::setLogoPath);
        }
        Organization saved = organizationService.save(organization);
        setupContext.setOrganizationId(saved.getId());
        return "redirect:/setup/branches";
    }

    @GetMapping("/branches")
    public String branchesStep(Model model, @ModelAttribute("setupContext") SetupContext setupContext) {
        if (!setupContext.hasOrganization()) {
            return "redirect:/setup/organization";
        }
        Optional<Organization> organization = organizationService.findById(setupContext.getOrganizationId());
        Branch branch = new Branch();
        organization.ifPresent(branch::setOrganization);
        model.addAttribute("branch", branch);
        model.addAttribute("organization", organization.orElse(null));
        model.addAttribute("branches", branchService.findByOrganization(setupContext.getOrganizationId()));
        model.addAttribute("currentStep", 2);
        return "setup-branches";
    }

    @PostMapping("/branches")
    public String saveBranch(@ModelAttribute Branch branch,
                             @ModelAttribute("setupContext") SetupContext setupContext) {
        if (!setupContext.hasOrganization()) {
            return "redirect:/setup/organization";
        }
        organizationService.findById(setupContext.getOrganizationId()).ifPresent(branch::setOrganization);
        branchService.save(branch);
        return "redirect:/setup/branches";
    }

    @GetMapping("/departments")
    public String departmentsStep(Model model, @ModelAttribute("setupContext") SetupContext setupContext) {
        if (!setupContext.hasOrganization()) {
            return "redirect:/setup/organization";
        }
        List<Branch> branches = branchService.findByOrganization(setupContext.getOrganizationId());
        Department department = new Department();
        model.addAttribute("branches", branches);
        model.addAttribute("department", department);
        model.addAttribute("departments", departmentService.findByBranches(branches.stream().map(Branch::getId).toList()));
        model.addAttribute("currentStep", 3);
        return "setup-departments";
    }

    @PostMapping("/departments")
    public String saveDepartment(@ModelAttribute Department department,
                                 @RequestParam("branchId") Long branchId,
                                 @ModelAttribute("setupContext") SetupContext setupContext) {
        if (!setupContext.hasOrganization()) {
            return "redirect:/setup/organization";
        }
        branchService.findById(branchId).ifPresent(department::setBranch);
        departmentService.save(department);
        return "redirect:/setup/departments";
    }

    @GetMapping("/hr")
    public String hrStep(Model model, @ModelAttribute("setupContext") SetupContext setupContext) {
        if (!setupContext.hasOrganization()) {
            return "redirect:/setup/organization";
        }
        List<Branch> branches = branchService.findByOrganization(setupContext.getOrganizationId());
        List<Long> branchIds = branches.stream().map(Branch::getId).toList();
        List<Department> departments = departmentService.findByBranches(branchIds);

        Employee hrEmployee = new Employee();
        hrEmployee.setRole(Role.HR_MANAGER);
        hrEmployee.setStatus(EmployeeStatus.ACTIVE);

        LeaveType leaveType = new LeaveType();
        leaveType.setRequiresHrApproval(true);
        leaveType.setRequiresManagerApproval(true);
        leaveType.setMaxDaysPerRequest(1);

        AttendanceRule attendanceRule = new AttendanceRule();
        organizationService.findById(setupContext.getOrganizationId()).ifPresent(attendanceRule::setOrganization);
        attendanceRule.setExpectedCheckIn(LocalTime.of(9, 0));
        attendanceRule.setExpectedCheckOut(LocalTime.of(17, 0));
        attendanceRule.setGraceMinutes(15);

        model.addAttribute("branches", branches);
        model.addAttribute("departments", departments);
        model.addAttribute("hrEmployee", hrEmployee);
        model.addAttribute("leaveType", leaveType);
        model.addAttribute("attendanceRule", attendanceRule);
        model.addAttribute("hrStaff", employeeService.findByRoles(List.of(Role.HR_MANAGER, Role.HR_EMPLOYEE)));
        model.addAttribute("leaveTypes", leaveTypeService.findAllActive());
        model.addAttribute("attendanceRules", attendanceRuleService.findAllActive());
        model.addAttribute("currentStep", 4);
        return "setup-hr";
    }

    @PostMapping("/hr/staff")
    public String saveHrStaff(@ModelAttribute("hrEmployee") Employee hrEmployee,
                              @RequestParam(value = "branchId", required = false) Long branchId,
                              @RequestParam(value = "departmentId", required = false) Long departmentId) {
        if (hrEmployee.getStatus() == null) {
            hrEmployee.setStatus(EmployeeStatus.ACTIVE);
        }
        if (hrEmployee.getPassword() != null && !hrEmployee.getPassword().isBlank()) {
            hrEmployee.setPassword(passwordEncoder.encode(hrEmployee.getPassword()));
        }
        if (branchId != null) {
            branchService.findById(branchId).ifPresent(hrEmployee::setBranch);
        }
        if (departmentId != null) {
            departmentService.findById(departmentId).ifPresent(hrEmployee::setDepartment);
        }
        employeeService.save(hrEmployee);
        return "redirect:/setup/hr";
    }

    @PostMapping("/hr/leave-type")
    public String saveLeaveType(@ModelAttribute("leaveType") LeaveType leaveType) {
        leaveTypeService.save(leaveType);
        return "redirect:/setup/hr";
    }

    @PostMapping("/hr/attendance-rule")
    public String saveAttendanceRule(@ModelAttribute("attendanceRule") AttendanceRule attendanceRule,
                                     @RequestParam(value = "branchId", required = false) Long branchId,
                                     @RequestParam(value = "departmentId", required = false) Long departmentId,
                                     @ModelAttribute("setupContext") SetupContext setupContext) {
        if (attendanceRule.getOrganization() == null && setupContext.hasOrganization()) {
            organizationService.findById(setupContext.getOrganizationId()).ifPresent(attendanceRule::setOrganization);
        }
        if (branchId != null) {
            branchService.findById(branchId).ifPresent(attendanceRule::setBranch);
        }
        if (departmentId != null) {
            departmentService.findById(departmentId).ifPresent(attendanceRule::setDepartment);
        }
        attendanceRuleService.save(attendanceRule);
        return "redirect:/setup/hr";
    }
}
