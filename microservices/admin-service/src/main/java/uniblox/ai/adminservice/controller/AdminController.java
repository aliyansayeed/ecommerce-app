package uniblox.ai.adminservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.adminservice.model.AdminReportResponse;
import uniblox.ai.adminservice.service.AdminService;
import uniblox.ai.common.model.dto.ApiResponse;

import static uniblox.ai.common.api.path.AdminApiPaths.*;

/**
 * Admin REST endpoints.
 * Base path: /api/v1/admin
 */
@RestController
@RequestMapping(API_BASE_PATH)
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Trigger discount generation for a user.
     */
    @PostMapping(USER_DISCOUNT_PATH)
    public ResponseEntity<ApiResponse<?>> generateDiscount(@PathVariable String userId) {
        ApiResponse<?> response = adminService.generateDiscount(userId);
        return buildResponse(response);
    }

    /**
     * Generate admin report.
     */
    @GetMapping(REPORTS_PATH)
    public ResponseEntity<ApiResponse<?>> getReport() {
        ApiResponse<AdminReportResponse> response = adminService.getReport();
        return buildResponse(response);
    }

    /**
     * Decide HTTP status based on ApiResponse.success().
     */
    private ResponseEntity<ApiResponse<?>> buildResponse(ApiResponse<?> response) {
        HttpStatus status = (response != null && response.success())
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
