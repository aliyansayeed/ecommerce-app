package uniblox.ai.common.api.path;

/**
 * Centralized constants for Admin module.
 * Only technical constants remain here.
 * Messages are externalized into messages.properties.
 */
public final class AdminApiPaths {

    private AdminApiPaths() {
        // utility class, no instantiation
    }

    // --- API base path ---
    public static final String API_BASE_PATH = "/api/v1/admin";

    // --- Endpoints ---
    public static final String USER_DISCOUNT_PATH = "/users/{userId}/discounts";
    public static final String REPORTS_PATH = "/reports";

    // --- Discount service relative paths ---
    public static final String DISCOUNT_GENERATE_PATH = "/generate/";
    public static final String DISCOUNT_ALL_PATH = "/all";
}
