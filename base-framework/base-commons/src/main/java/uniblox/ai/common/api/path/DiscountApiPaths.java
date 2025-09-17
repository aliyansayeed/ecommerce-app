package uniblox.ai.common.api.path;

/**
 * Centralized constants for Discount module.
 * Only technical constants remain here.
 * Messages are externalized into messages.properties.
 */
public final class DiscountApiPaths {

    private DiscountApiPaths() {
        // utility class, no instantiation
    }

    // --- API base path ---
    public static final String API_BASE_PATH = "/api/v1/discounts";

    // --- Endpoints ---
    public static final String GENERATE_PATH = "/generate/{userId}";
    public static final String ALL_PATH = "/all";
    public static final String BY_ID_PATH = "/{discountId}";
}
