package uniblox.ai.common.api.path;

/**
 * Centralized constants for Order module.
 * Only technical constants remain here.
 * Messages are externalized into messages.properties.
 */
public final class OrderApiPaths {

    private OrderApiPaths() {
        // utility class, no instantiation
    }

    // --- API base path ---
    public static final String API_BASE_PATH = "/api/v1/orders";

    // --- Endpoints ---
    public static final String CREATE_PATH = "/";
    public static final String BY_ID_PATH = "/{orderId}";
    public static final String ALL_PATH = "/all";
    public static final String BY_USER_PATH = "/user/{userId}";
    public static final String STATUS_UPDATE_PATH = "/{orderId}/status";
}
