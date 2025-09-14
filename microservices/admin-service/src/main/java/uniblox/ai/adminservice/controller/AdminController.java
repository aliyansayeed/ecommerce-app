package uniblox.ai.adminservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.adminservice.dto.AdminReportResponseDto;
import uniblox.ai.adminservice.service.AdminService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Trigger discount generation for a user.
     */
    @PostMapping("/discounts/generate/{userId}")
    public ResponseEntity<Object> generateDiscount(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.generateDiscount(userId));
    }

    /**
     * Admin report with totals and discounts.
     */
    @GetMapping("/report")
    public ResponseEntity<AdminReportResponseDto> getReport() {
        return ResponseEntity.ok(adminService.getReport());
    }
}
