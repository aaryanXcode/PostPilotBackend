package com.back.postpilot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // ========== PUBLIC ENDPOINTS ==========

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicData() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is public data - no authentication required");
        response.put("data", Arrays.asList("Item 1", "Item 2", "Item 3"));
        response.put("timestamp", new Date());
        response.put("access_level", "PUBLIC");
        return ResponseEntity.ok(response);
    }

    // ========== AUTHENTICATED USER ENDPOINTS ==========

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> getProtectedData(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This data requires authentication");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("data", Arrays.asList("Protected Item 1", "Protected Item 2"));
        response.put("timestamp", new Date());
        response.put("access_level", "AUTHENTICATED");
        return ResponseEntity.ok(response);
    }

    // ========== USER ROLE ENDPOINTS ==========

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getUserData() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This data is for regular users only");
        response.put("data", Map.of(
                "dashboard", "User Dashboard",
                "features", Arrays.asList("View Profile", "Update Settings", "View History"),
                "limits", Map.of(
                        "downloads", 10,
                        "uploads", 5,
                        "storage_mb", 100
                )
        ));
        response.put("access_level", "USER");
        return ResponseEntity.ok(response);
    }

    // ========== ADMIN ROLE ENDPOINTS ==========

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminData() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This data is for admins only");
        response.put("data", Map.of(
                "dashboard", "Admin Dashboard",
                "features", Arrays.asList("Manage Users", "View Reports", "System Settings", "Bulk Operations"),
                "stats", Map.of(
                        "total_users", 150,
                        "active_sessions", 23,
                        "pending_requests", 7
                ),
                "permissions", Arrays.asList("READ", "WRITE", "DELETE", "MANAGE_USERS")
        ));
        response.put("access_level", "ADMIN");
        return ResponseEntity.ok(response);
    }

    // ========== SUPER ADMIN ROLE ENDPOINTS ==========

    @GetMapping("/super-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSuperAdminData() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This data is for super admins only");
        response.put("data", Map.of(
                "dashboard", "Super Admin Dashboard",
                "features", Arrays.asList("System Control", "Admin Management", "Security Settings", "Database Access"),
                "system_info", Map.of(
                        "server_status", "HEALTHY",
                        "database_connections", 45,
                        "memory_usage", "67%",
                        "uptime_hours", 168
                ),
                "critical_operations", Arrays.asList("Database Backup", "System Maintenance", "Security Audit"),
                "permissions", Arrays.asList("ALL_PERMISSIONS", "SYSTEM_CONTROL", "ADMIN_MANAGEMENT")
        ));
        response.put("access_level", "SUPER_ADMIN");
        return ResponseEntity.ok(response);
    }

    // ========== MULTI-ROLE ENDPOINTS ==========

    @GetMapping("/admin-or-super")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminOrSuperAdminData(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This data is for admins and super admins");
        response.put("current_user", auth.getName());
        response.put("user_role", auth.getAuthorities());
        response.put("data", Map.of(
                "shared_features", Arrays.asList("User Management", "Reports", "Analytics"),
                "recent_activities", Arrays.asList(
                        "User 'john123' registered",
                        "Admin 'admin1' updated settings",
                        "System backup completed"
                ),
                "notifications", Arrays.asList(
                        "5 new user registrations today",
                        "System update available",
                        "Monthly report ready"
                )
        ));
        response.put("access_level", "ADMIN_OR_SUPER_ADMIN");
        return ResponseEntity.ok(response);
    }

    // ========== ROLE HIERARCHY TEST ==========

    @GetMapping("/hierarchy-test")
    public ResponseEntity<Map<String, Object>> getRoleHierarchyTest(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());

        // Check what the user can access
        Map<String, Boolean> accessMap = new HashMap<>();
        boolean hasUserRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        boolean hasAdminRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean hasSuperAdminRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        accessMap.put("can_access_user_endpoints", hasUserRole);
        accessMap.put("can_access_admin_endpoints", hasAdminRole);
        accessMap.put("can_access_super_admin_endpoints", hasSuperAdminRole);

        response.put("access_matrix", accessMap);
        response.put("recommended_endpoints", getRecommendedEndpoints(hasUserRole, hasAdminRole, hasSuperAdminRole));

        return ResponseEntity.ok(response);
    }

    // ========== HELPER METHODS ==========

    private List<String> getRecommendedEndpoints(boolean isUser, boolean isAdmin, boolean isSuperAdmin) {
        List<String> endpoints = new ArrayList<>();
        endpoints.add("/api/test/public");
        endpoints.add("/api/test/protected");

        if (isUser) {
            endpoints.add("/api/test/user");
        }
        if (isAdmin) {
            endpoints.add("/api/test/admin");
            endpoints.add("/api/test/admin-or-super");
        }
        if (isSuperAdmin) {
            endpoints.add("/api/test/super-admin");
            endpoints.add("/api/test/admin-or-super");
        }

        return endpoints;
    }
}